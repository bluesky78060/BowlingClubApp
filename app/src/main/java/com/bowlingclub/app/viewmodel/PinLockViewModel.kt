package com.bowlingclub.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowlingclub.app.data.local.dao.SettingDao
import com.bowlingclub.app.data.local.entity.Setting
import com.bowlingclub.app.util.SecureStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

data class PinLockUiState(
    val pin: String = "",
    val confirmPin: String = "",
    val mode: PinLockMode = PinLockMode.VERIFY,
    val step: PinSetupStep = PinSetupStep.ENTER,
    val error: String? = null,
    val isUnlocked: Boolean = false,
    val isPinEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isOldPinVerified: Boolean = false,
    val isLockedOut: Boolean = false,
    val lockoutRemainingSeconds: Int = 0
)

enum class PinLockMode {
    VERIFY, SETUP, CHANGE, DISABLE
}

enum class PinSetupStep {
    ENTER, CONFIRM
}

@HiltViewModel
class PinLockViewModel @Inject constructor(
    private val settingDao: SettingDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinLockUiState())
    val uiState: StateFlow<PinLockUiState> = _uiState.asStateFlow()

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_ENABLED = "pin_enabled"
        private const val KEY_FAIL_COUNT = "pin_fail_count"
        private const val KEY_LOCKOUT_UNTIL = "pin_lockout_until"
        private const val PIN_LENGTH = 4
        private const val MAX_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 30_000L
        private const val PBKDF2_ITERATIONS = 10_000
        private const val HASH_LENGTH = 256
    }

    init {
        loadPinStatus()
    }

    private fun loadPinStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val isPinEnabled = settingDao.getSettingValue(KEY_PIN_ENABLED) == "true"
                _uiState.update {
                    it.copy(isPinEnabled = isPinEnabled, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "PIN 상태를 불러오는데 실패했습니다: ${e.message}", isLoading = false)
                }
            }
        }
    }

    private fun isLockedOut(): Boolean {
        val lockoutUntil = SecureStorage.getString(context, KEY_LOCKOUT_UNTIL, "0").toLongOrNull() ?: 0L
        return System.currentTimeMillis() < lockoutUntil
    }

    private fun getFailCount(): Int {
        return SecureStorage.getString(context, KEY_FAIL_COUNT, "0").toIntOrNull() ?: 0
    }

    private fun incrementFailCount() {
        val count = getFailCount() + 1
        SecureStorage.saveString(context, KEY_FAIL_COUNT, count.toString())
        if (count >= MAX_ATTEMPTS) {
            val lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            SecureStorage.saveString(context, KEY_LOCKOUT_UNTIL, lockoutUntil.toString())
            SecureStorage.saveString(context, KEY_FAIL_COUNT, "0")
            startLockoutTimer()
        }
    }

    private fun resetFailCount() {
        SecureStorage.saveString(context, KEY_FAIL_COUNT, "0")
        SecureStorage.saveString(context, KEY_LOCKOUT_UNTIL, "0")
    }

    private fun startLockoutTimer() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLockedOut = true, error = null) }
            val lockoutUntil = SecureStorage.getString(context, KEY_LOCKOUT_UNTIL, "0").toLongOrNull() ?: 0L
            while (System.currentTimeMillis() < lockoutUntil) {
                val remaining = ((lockoutUntil - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
                _uiState.update { it.copy(lockoutRemainingSeconds = remaining) }
                delay(1000)
            }
            _uiState.update { it.copy(isLockedOut = false, lockoutRemainingSeconds = 0) }
        }
    }

    fun appendDigit(digit: Int) {
        if (digit !in 0..9) return
        if (isLockedOut()) return

        _uiState.update { state ->
            val currentPin = when (state.step) {
                PinSetupStep.ENTER -> state.pin
                PinSetupStep.CONFIRM -> state.confirmPin
            }

            if (currentPin.length >= PIN_LENGTH) {
                return@update state
            }

            val newPin = currentPin + digit.toString()

            val updatedState = when (state.step) {
                PinSetupStep.ENTER -> state.copy(pin = newPin, error = null)
                PinSetupStep.CONFIRM -> state.copy(confirmPin = newPin, error = null)
            }

            if (newPin.length == PIN_LENGTH) {
                when (state.mode) {
                    PinLockMode.VERIFY -> verifyPinInternal(updatedState)
                    PinLockMode.SETUP -> {
                        when (state.step) {
                            PinSetupStep.ENTER -> updatedState.copy(step = PinSetupStep.CONFIRM, confirmPin = "")
                            PinSetupStep.CONFIRM -> setupPinInternal(updatedState)
                        }
                    }
                    PinLockMode.CHANGE -> {
                        when (state.step) {
                            PinSetupStep.ENTER -> {
                                if (!state.isOldPinVerified) {
                                    verifyOldPinForChange(updatedState)
                                } else {
                                    updatedState.copy(step = PinSetupStep.CONFIRM, confirmPin = "")
                                }
                            }
                            PinSetupStep.CONFIRM -> setupPinInternal(updatedState)
                        }
                    }
                    PinLockMode.DISABLE -> disablePinInternal(updatedState)
                }
            } else {
                updatedState
            }
        }
    }

    fun deleteDigit() {
        _uiState.update { state ->
            when (state.step) {
                PinSetupStep.ENTER -> {
                    if (state.pin.isNotEmpty()) state.copy(pin = state.pin.dropLast(1), error = null)
                    else state
                }
                PinSetupStep.CONFIRM -> {
                    if (state.confirmPin.isNotEmpty()) state.copy(confirmPin = state.confirmPin.dropLast(1), error = null)
                    else state
                }
            }
        }
    }

    private fun verifyPinInternal(state: PinLockUiState): PinLockUiState {
        viewModelScope.launch {
            try {
                val storedHash = SecureStorage.getString(context, KEY_PIN_HASH, "")
                val storedSalt = SecureStorage.getString(context, KEY_PIN_SALT, "")
                if (storedHash.isEmpty() || storedSalt.isEmpty()) {
                    _uiState.update { it.copy(error = "저장된 PIN이 없습니다", pin = "") }
                    return@launch
                }

                val salt = hexToBytes(storedSalt)
                val inputHash = hashPin(state.pin, salt)
                if (inputHash == storedHash) {
                    resetFailCount()
                    _uiState.update { it.copy(isUnlocked = true, error = null) }
                } else {
                    incrementFailCount()
                    val remaining = MAX_ATTEMPTS - getFailCount()
                    val errorMsg = if (remaining > 0) {
                        "PIN이 일치하지 않습니다 (${remaining}회 남음)"
                    } else {
                        "PIN이 일치하지 않습니다"
                    }
                    _uiState.update { it.copy(error = errorMsg, pin = "") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "PIN 확인에 실패했습니다: ${e.message}", pin = "") }
            }
        }
        return state
    }

    private fun setupPinInternal(state: PinLockUiState): PinLockUiState {
        viewModelScope.launch {
            try {
                if (state.pin != state.confirmPin) {
                    _uiState.update {
                        it.copy(error = "PIN이 일치하지 않습니다. 다시 설정해주세요.", pin = "", confirmPin = "", step = PinSetupStep.ENTER)
                    }
                    return@launch
                }

                val salt = generateSalt()
                val hash = hashPin(state.pin, salt)
                SecureStorage.saveString(context, KEY_PIN_HASH, hash)
                SecureStorage.saveString(context, KEY_PIN_SALT, bytesToHex(salt))
                settingDao.insertOrUpdate(Setting(KEY_PIN_ENABLED, "true"))

                _uiState.update { it.copy(isPinEnabled = true, isUnlocked = true, error = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "PIN 설정에 실패했습니다: ${e.message}", pin = "", confirmPin = "", step = PinSetupStep.ENTER)
                }
            }
        }
        return state
    }

    private fun disablePinInternal(state: PinLockUiState): PinLockUiState {
        viewModelScope.launch {
            try {
                val storedHash = SecureStorage.getString(context, KEY_PIN_HASH, "")
                val storedSalt = SecureStorage.getString(context, KEY_PIN_SALT, "")
                if (storedHash.isEmpty() || storedSalt.isEmpty()) {
                    _uiState.update { it.copy(error = "저장된 PIN이 없습니다", pin = "") }
                    return@launch
                }

                val salt = hexToBytes(storedSalt)
                val inputHash = hashPin(state.pin, salt)
                if (inputHash == storedHash) {
                    SecureStorage.saveString(context, KEY_PIN_HASH, "")
                    SecureStorage.saveString(context, KEY_PIN_SALT, "")
                    settingDao.insertOrUpdate(Setting(KEY_PIN_ENABLED, "false"))
                    resetFailCount()

                    _uiState.update { it.copy(isPinEnabled = false, isUnlocked = true, error = null) }
                } else {
                    incrementFailCount()
                    _uiState.update { it.copy(error = "PIN이 일치하지 않습니다", pin = "") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "PIN 비활성화에 실패했습니다: ${e.message}", pin = "") }
            }
        }
        return state
    }

    private fun verifyOldPinForChange(state: PinLockUiState): PinLockUiState {
        viewModelScope.launch {
            try {
                val storedHash = SecureStorage.getString(context, KEY_PIN_HASH, "")
                val storedSalt = SecureStorage.getString(context, KEY_PIN_SALT, "")
                if (storedHash.isEmpty() || storedSalt.isEmpty()) {
                    _uiState.update { it.copy(error = "저장된 PIN이 없습니다", pin = "") }
                    return@launch
                }
                val salt = hexToBytes(storedSalt)
                val inputHash = hashPin(state.pin, salt)
                if (inputHash == storedHash) {
                    resetFailCount()
                    _uiState.update { it.copy(isOldPinVerified = true, pin = "", error = null) }
                } else {
                    incrementFailCount()
                    _uiState.update { it.copy(error = "PIN이 일치하지 않습니다", pin = "") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "PIN 확인에 실패했습니다: ${e.message}", pin = "") }
            }
        }
        return state
    }

    fun enablePin() {
        _uiState.update {
            it.copy(mode = PinLockMode.SETUP, step = PinSetupStep.ENTER, pin = "", confirmPin = "", error = null, isUnlocked = false)
        }
    }

    fun disablePin() {
        _uiState.update {
            it.copy(mode = PinLockMode.DISABLE, step = PinSetupStep.ENTER, pin = "", confirmPin = "", error = null, isUnlocked = false)
        }
    }

    fun changePin() {
        _uiState.update {
            it.copy(mode = PinLockMode.CHANGE, step = PinSetupStep.ENTER, pin = "", confirmPin = "", error = null, isUnlocked = false, isOldPinVerified = false)
        }
    }

    fun setMode(mode: PinLockMode) {
        _uiState.update {
            it.copy(mode = mode, step = PinSetupStep.ENTER, pin = "", confirmPin = "", error = null, isUnlocked = false, isOldPinVerified = false)
        }
    }

    fun isPinSet(): Flow<Boolean> {
        return settingDao.getSetting(KEY_PIN_ENABLED).map { setting ->
            setting?.value == "true"
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun hashPin(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hashBytes = factory.generateSecret(spec).encoded
        return bytesToHex(hashBytes)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hexToBytes(hex: String): ByteArray {
        return ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }
}
