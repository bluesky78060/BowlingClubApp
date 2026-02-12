package com.bowlingclub.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowlingclub.app.data.local.dao.SettingDao
import com.bowlingclub.app.data.local.entity.Setting
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

data class PinLockUiState(
    val pin: String = "",           // 현재 입력된 PIN
    val confirmPin: String = "",    // 확인용 PIN (설정 모드)
    val mode: PinLockMode = PinLockMode.VERIFY,
    val step: PinSetupStep = PinSetupStep.ENTER,
    val error: String? = null,
    val isUnlocked: Boolean = false,
    val isPinEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isOldPinVerified: Boolean = false  // CHANGE 모드에서 기존 PIN 확인 완료 여부
)

enum class PinLockMode {
    VERIFY,     // PIN 확인 (앱 잠금 해제)
    SETUP,      // PIN 설정 (새로 설정)
    CHANGE,     // PIN 변경 (기존 확인 후 새로 설정)
    DISABLE     // PIN 비활성화 (기존 확인 후 삭제)
}

enum class PinSetupStep {
    ENTER,      // PIN 입력
    CONFIRM     // PIN 확인 (설정 모드)
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
        private const val KEY_PIN_ENABLED = "pin_enabled"
        private const val PIN_LENGTH = 4
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
                    it.copy(
                        isPinEnabled = isPinEnabled,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "PIN 상태를 불러오는데 실패했습니다: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun appendDigit(digit: Int) {
        if (digit !in 0..9) return

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

            // 4자리 입력 완료 시 자동 검증/진행
            if (newPin.length == PIN_LENGTH) {
                when (state.mode) {
                    PinLockMode.VERIFY -> {
                        verifyPinInternal(updatedState)
                    }
                    PinLockMode.SETUP -> {
                        when (state.step) {
                            PinSetupStep.ENTER -> {
                                updatedState.copy(
                                    step = PinSetupStep.CONFIRM,
                                    confirmPin = ""
                                )
                            }
                            PinSetupStep.CONFIRM -> {
                                setupPinInternal(updatedState)
                            }
                        }
                    }
                    PinLockMode.CHANGE -> {
                        when (state.step) {
                            PinSetupStep.ENTER -> {
                                if (!state.isOldPinVerified) {
                                    // 기존 PIN 확인
                                    verifyOldPinForChange(updatedState)
                                } else {
                                    // 기존 PIN 확인 완료, 새 PIN 입력 완료 → CONFIRM으로 이동
                                    updatedState.copy(
                                        step = PinSetupStep.CONFIRM,
                                        confirmPin = ""
                                    )
                                }
                            }
                            PinSetupStep.CONFIRM -> {
                                setupPinInternal(updatedState)
                            }
                        }
                    }
                    PinLockMode.DISABLE -> {
                        disablePinInternal(updatedState)
                    }
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
                    if (state.pin.isNotEmpty()) {
                        state.copy(pin = state.pin.dropLast(1), error = null)
                    } else {
                        state
                    }
                }
                PinSetupStep.CONFIRM -> {
                    if (state.confirmPin.isNotEmpty()) {
                        state.copy(confirmPin = state.confirmPin.dropLast(1), error = null)
                    } else {
                        state
                    }
                }
            }
        }
    }

    private fun verifyPinInternal(state: PinLockUiState): PinLockUiState {
        viewModelScope.launch {
            try {
                val storedHash = settingDao.getSettingValue(KEY_PIN_HASH)
                if (storedHash == null) {
                    _uiState.update {
                        it.copy(
                            error = "저장된 PIN이 없습니다",
                            pin = ""
                        )
                    }
                    return@launch
                }

                val inputHash = hashPin(state.pin)
                if (inputHash == storedHash) {
                    _uiState.update {
                        it.copy(
                            isUnlocked = true,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "PIN이 일치하지 않습니다",
                            pin = ""
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "PIN 확인에 실패했습니다: ${e.message}",
                        pin = ""
                    )
                }
            }
        }
        return state
    }

    private fun setupPinInternal(state: PinLockUiState): PinLockUiState {
        viewModelScope.launch {
            try {
                if (state.pin != state.confirmPin) {
                    _uiState.update {
                        it.copy(
                            error = "PIN이 일치하지 않습니다. 다시 설정해주세요.",
                            pin = "",
                            confirmPin = "",
                            step = PinSetupStep.ENTER
                        )
                    }
                    return@launch
                }

                val hash = hashPin(state.pin)
                settingDao.insertOrUpdate(Setting(KEY_PIN_HASH, hash))
                settingDao.insertOrUpdate(Setting(KEY_PIN_ENABLED, "true"))

                _uiState.update {
                    it.copy(
                        isPinEnabled = true,
                        isUnlocked = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "PIN 설정에 실패했습니다: ${e.message}",
                        pin = "",
                        confirmPin = "",
                        step = PinSetupStep.ENTER
                    )
                }
            }
        }
        return state
    }

    private fun disablePinInternal(state: PinLockUiState): PinLockUiState {
        viewModelScope.launch {
            try {
                val storedHash = settingDao.getSettingValue(KEY_PIN_HASH)
                if (storedHash == null) {
                    _uiState.update {
                        it.copy(
                            error = "저장된 PIN이 없습니다",
                            pin = ""
                        )
                    }
                    return@launch
                }

                val inputHash = hashPin(state.pin)
                if (inputHash == storedHash) {
                    settingDao.delete(KEY_PIN_HASH)
                    settingDao.insertOrUpdate(Setting(KEY_PIN_ENABLED, "false"))

                    _uiState.update {
                        it.copy(
                            isPinEnabled = false,
                            isUnlocked = true,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "PIN이 일치하지 않습니다",
                            pin = ""
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "PIN 비활성화에 실패했습니다: ${e.message}",
                        pin = ""
                    )
                }
            }
        }
        return state
    }

    private fun verifyOldPinForChange(state: PinLockUiState): PinLockUiState {
        viewModelScope.launch {
            try {
                val storedHash = settingDao.getSettingValue(KEY_PIN_HASH)
                if (storedHash == null) {
                    _uiState.update { it.copy(error = "저장된 PIN이 없습니다", pin = "") }
                    return@launch
                }
                val inputHash = hashPin(state.pin)
                if (inputHash == storedHash) {
                    _uiState.update {
                        it.copy(
                            isOldPinVerified = true,
                            pin = "",
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(error = "PIN이 일치하지 않습니다", pin = "")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "PIN 확인에 실패했습니다: ${e.message}", pin = "")
                }
            }
        }
        return state
    }

    fun enablePin() {
        _uiState.update {
            it.copy(
                mode = PinLockMode.SETUP,
                step = PinSetupStep.ENTER,
                pin = "",
                confirmPin = "",
                error = null,
                isUnlocked = false
            )
        }
    }

    fun disablePin() {
        _uiState.update {
            it.copy(
                mode = PinLockMode.DISABLE,
                step = PinSetupStep.ENTER,
                pin = "",
                confirmPin = "",
                error = null,
                isUnlocked = false
            )
        }
    }

    fun changePin() {
        _uiState.update {
            it.copy(
                mode = PinLockMode.CHANGE,
                step = PinSetupStep.ENTER,
                pin = "",
                confirmPin = "",
                error = null,
                isUnlocked = false,
                isOldPinVerified = false
            )
        }
    }

    fun setMode(mode: PinLockMode) {
        _uiState.update {
            it.copy(
                mode = mode,
                step = PinSetupStep.ENTER,
                pin = "",
                confirmPin = "",
                error = null,
                isUnlocked = false,
                isOldPinVerified = false
            )
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

    private fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(pin.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
