package com.bowlingclub.app.viewmodel

import android.content.Context
import com.bowlingclub.app.MainDispatcherRule
import com.bowlingclub.app.data.local.dao.SettingDao
import com.bowlingclub.app.data.local.entity.Setting
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.security.MessageDigest

@OptIn(ExperimentalCoroutinesApi::class)
class PinLockViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settingDao: SettingDao = mock()
    private val context: Context = mock()
    private lateinit var viewModel: PinLockViewModel

    private fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(pin.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    @Before
    fun setup() = runTest {
        // Default: PIN is not enabled
        whenever(settingDao.getSettingValue("pin_enabled")).thenReturn("false")
        whenever(settingDao.getSetting("pin_enabled")).thenReturn(flowOf(Setting("pin_enabled", "false")))
    }

    // ── appendDigit Tests ──────────────────────────────────────

    @Test
    fun appendDigit_addsDigitToPin_whenDigitIsValid() = runTest {
        viewModel = PinLockViewModel(settingDao, context)
        advanceUntilIdle()

        viewModel.appendDigit(1)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.pin).isEqualTo("1")
    }

    @Test
    fun appendDigit_addsMultipleDigits_upToFourDigits() = runTest {
        viewModel = PinLockViewModel(settingDao, context)
        advanceUntilIdle()

        viewModel.appendDigit(1)
        viewModel.appendDigit(2)
        viewModel.appendDigit(3)
        viewModel.appendDigit(4)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.pin).isEqualTo("1234")
    }

    @Test
    fun appendDigit_ignoresDigits_greaterThanNine() = runTest {
        viewModel = PinLockViewModel(settingDao, context)
        advanceUntilIdle()

        viewModel.appendDigit(10)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.pin).isEmpty()
    }

    @Test
    fun appendDigit_ignoresDigits_lessThanZero() = runTest {
        viewModel = PinLockViewModel(settingDao, context)
        advanceUntilIdle()

        viewModel.appendDigit(-1)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.pin).isEmpty()
    }

    @Test
    fun appendDigit_stopsAtFourDigits_whenMoreDigitsAppended() = runTest {
        viewModel = PinLockViewModel(settingDao, context)
        advanceUntilIdle()

        viewModel.appendDigit(1)
        viewModel.appendDigit(2)
        viewModel.appendDigit(3)
        viewModel.appendDigit(4)
        viewModel.appendDigit(5) // Should be ignored
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.pin).isEqualTo("1234")
    }

    // ── deleteDigit Tests ──────────────────────────────────────

    @Test
    fun deleteDigit_removesLastDigit_whenPinNotEmpty() = runTest {
        viewModel = PinLockViewModel(settingDao, context)
        advanceUntilIdle()

        viewModel.appendDigit(1)
        viewModel.appendDigit(2)
        viewModel.appendDigit(3)
        advanceUntilIdle()

        viewModel.deleteDigit()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.pin).isEqualTo("12")
    }

    @Test
    fun deleteDigit_noOp_whenPinIsEmpty() = runTest {
        viewModel = PinLockViewModel(settingDao, context)
        advanceUntilIdle()

        viewModel.deleteDigit()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.pin).isEmpty()
    }

    // ── VERIFY Mode Tests ──────────────────────────────────────

    @Test
    fun verifyMode_fourDigitsTriggerVerification_success() = runTest {
        val correctPin = "1234"
        val hash = hashPin(correctPin)
        whenever(settingDao.getSettingValue("pin_hash")).thenReturn(hash)

        viewModel = PinLockViewModel(settingDao, context)
        viewModel.setMode(PinLockMode.VERIFY)
        advanceUntilIdle()

        viewModel.appendDigit(1)
        viewModel.appendDigit(2)
        viewModel.appendDigit(3)
        viewModel.appendDigit(4)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isUnlocked).isTrue()
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun verifyMode_fourDigitsTriggerVerification_failure() = runTest {
        val correctPin = "1234"
        val hash = hashPin(correctPin)
        whenever(settingDao.getSettingValue("pin_hash")).thenReturn(hash)

        viewModel = PinLockViewModel(settingDao, context)
        viewModel.setMode(PinLockMode.VERIFY)
        advanceUntilIdle()

        viewModel.appendDigit(5)
        viewModel.appendDigit(6)
        viewModel.appendDigit(7)
        viewModel.appendDigit(8)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isUnlocked).isFalse()
        assertThat(viewModel.uiState.value.error).isEqualTo("PIN이 일치하지 않습니다")
        assertThat(viewModel.uiState.value.pin).isEmpty() // PIN cleared after failure
    }

    // ── SETUP Mode Tests ───────────────────────────────────────

    @Test
    fun setupMode_enterStepTransitionsToConfirmStep_whenFourDigitsEntered() = runTest {
        viewModel = PinLockViewModel(settingDao, context)
        viewModel.setMode(PinLockMode.SETUP)
        advanceUntilIdle()

        viewModel.appendDigit(1)
        viewModel.appendDigit(2)
        viewModel.appendDigit(3)
        viewModel.appendDigit(4)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.step).isEqualTo(PinSetupStep.CONFIRM)
        assertThat(viewModel.uiState.value.pin).isEqualTo("1234")
        assertThat(viewModel.uiState.value.confirmPin).isEmpty()
    }

    @Test
    fun setupMode_matchingPins_success() = runTest {
        viewModel = PinLockViewModel(settingDao, context)
        viewModel.setMode(PinLockMode.SETUP)
        advanceUntilIdle()

        // Enter PIN
        viewModel.appendDigit(1)
        viewModel.appendDigit(2)
        viewModel.appendDigit(3)
        viewModel.appendDigit(4)
        advanceUntilIdle()

        // Confirm PIN
        viewModel.appendDigit(1)
        viewModel.appendDigit(2)
        viewModel.appendDigit(3)
        viewModel.appendDigit(4)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isPinEnabled).isTrue()
        assertThat(viewModel.uiState.value.isUnlocked).isTrue()
        assertThat(viewModel.uiState.value.error).isNull()
        verify(settingDao).insertOrUpdate(eq(Setting("pin_hash", hashPin("1234"))))
        verify(settingDao).insertOrUpdate(eq(Setting("pin_enabled", "true")))
    }

    @Test
    fun setupMode_mismatchingPins_error() = runTest {
        viewModel = PinLockViewModel(settingDao, context)
        viewModel.setMode(PinLockMode.SETUP)
        advanceUntilIdle()

        // Enter PIN
        viewModel.appendDigit(1)
        viewModel.appendDigit(2)
        viewModel.appendDigit(3)
        viewModel.appendDigit(4)
        advanceUntilIdle()

        // Confirm different PIN
        viewModel.appendDigit(5)
        viewModel.appendDigit(6)
        viewModel.appendDigit(7)
        viewModel.appendDigit(8)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isEqualTo("PIN이 일치하지 않습니다. 다시 설정해주세요.")
        assertThat(viewModel.uiState.value.pin).isEmpty()
        assertThat(viewModel.uiState.value.confirmPin).isEmpty()
        assertThat(viewModel.uiState.value.step).isEqualTo(PinSetupStep.ENTER) // Reset to ENTER
    }

    // ── CHANGE Mode Tests ──────────────────────────────────────

    @Test
    fun changeMode_oldPinVerificationRequired_success() = runTest {
        val oldPin = "1234"
        val hash = hashPin(oldPin)
        whenever(settingDao.getSettingValue("pin_hash")).thenReturn(hash)

        viewModel = PinLockViewModel(settingDao, context)
        viewModel.setMode(PinLockMode.CHANGE)
        advanceUntilIdle()

        // Verify old PIN
        viewModel.appendDigit(1)
        viewModel.appendDigit(2)
        viewModel.appendDigit(3)
        viewModel.appendDigit(4)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOldPinVerified).isTrue()
        assertThat(viewModel.uiState.value.pin).isEmpty() // PIN cleared after verification
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun changeMode_oldPinVerificationRequired_failure() = runTest {
        val oldPin = "1234"
        val hash = hashPin(oldPin)
        whenever(settingDao.getSettingValue("pin_hash")).thenReturn(hash)

        viewModel = PinLockViewModel(settingDao, context)
        viewModel.setMode(PinLockMode.CHANGE)
        advanceUntilIdle()

        // Wrong old PIN
        viewModel.appendDigit(5)
        viewModel.appendDigit(6)
        viewModel.appendDigit(7)
        viewModel.appendDigit(8)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOldPinVerified).isFalse()
        assertThat(viewModel.uiState.value.error).isEqualTo("PIN이 일치하지 않습니다")
    }

    @Test
    fun changeMode_afterOldPinVerified_newPinSetupWorks() = runTest {
        val oldPin = "1234"
        val hash = hashPin(oldPin)
        whenever(settingDao.getSettingValue("pin_hash")).thenReturn(hash)

        viewModel = PinLockViewModel(settingDao, context)
        viewModel.setMode(PinLockMode.CHANGE)
        advanceUntilIdle()

        // Verify old PIN
        viewModel.appendDigit(1)
        viewModel.appendDigit(2)
        viewModel.appendDigit(3)
        viewModel.appendDigit(4)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOldPinVerified).isTrue()

        // Enter new PIN
        viewModel.appendDigit(9)
        viewModel.appendDigit(8)
        viewModel.appendDigit(7)
        viewModel.appendDigit(6)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.step).isEqualTo(PinSetupStep.CONFIRM)

        // Confirm new PIN
        viewModel.appendDigit(9)
        viewModel.appendDigit(8)
        viewModel.appendDigit(7)
        viewModel.appendDigit(6)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isPinEnabled).isTrue()
        assertThat(viewModel.uiState.value.isUnlocked).isTrue()
        verify(settingDao).insertOrUpdate(eq(Setting("pin_hash", hashPin("9876"))))
        verify(settingDao).insertOrUpdate(eq(Setting("pin_enabled", "true")))
    }

    // ── DISABLE Mode Tests ─────────────────────────────────────

    @Test
    fun disableMode_correctPin_success() = runTest {
        val correctPin = "1234"
        val hash = hashPin(correctPin)
        whenever(settingDao.getSettingValue("pin_hash")).thenReturn(hash)

        viewModel = PinLockViewModel(settingDao, context)
        viewModel.setMode(PinLockMode.DISABLE)
        advanceUntilIdle()

        viewModel.appendDigit(1)
        viewModel.appendDigit(2)
        viewModel.appendDigit(3)
        viewModel.appendDigit(4)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isPinEnabled).isFalse()
        assertThat(viewModel.uiState.value.isUnlocked).isTrue()
        verify(settingDao).delete("pin_hash")
        verify(settingDao).insertOrUpdate(eq(Setting("pin_enabled", "false")))
    }

    @Test
    fun disableMode_incorrectPin_failure() = runTest {
        val correctPin = "1234"
        val hash = hashPin(correctPin)
        whenever(settingDao.getSettingValue("pin_hash")).thenReturn(hash)

        viewModel = PinLockViewModel(settingDao, context)
        viewModel.setMode(PinLockMode.DISABLE)
        advanceUntilIdle()

        viewModel.appendDigit(5)
        viewModel.appendDigit(6)
        viewModel.appendDigit(7)
        viewModel.appendDigit(8)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isPinEnabled).isFalse() // Remains false
        assertThat(viewModel.uiState.value.isUnlocked).isFalse()
        assertThat(viewModel.uiState.value.error).isEqualTo("PIN이 일치하지 않습니다")
    }

    // ── setMode Tests ──────────────────────────────────────────

    @Test
    fun setMode_properlyInitializesMode() = runTest {
        viewModel = PinLockViewModel(settingDao, context)
        advanceUntilIdle()

        viewModel.setMode(PinLockMode.SETUP)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.mode).isEqualTo(PinLockMode.SETUP)
        assertThat(state.step).isEqualTo(PinSetupStep.ENTER)
        assertThat(state.pin).isEmpty()
        assertThat(state.confirmPin).isEmpty()
        assertThat(state.error).isNull()
        assertThat(state.isUnlocked).isFalse()
        assertThat(state.isOldPinVerified).isFalse()
    }

    // ── clearError Tests ───────────────────────────────────────

    @Test
    fun clearError_clearsErrorState() = runTest {
        val correctPin = "1234"
        val hash = hashPin(correctPin)
        whenever(settingDao.getSettingValue("pin_hash")).thenReturn(hash)

        viewModel = PinLockViewModel(settingDao, context)
        viewModel.setMode(PinLockMode.VERIFY)
        advanceUntilIdle()

        // Trigger error
        viewModel.appendDigit(5)
        viewModel.appendDigit(6)
        viewModel.appendDigit(7)
        viewModel.appendDigit(8)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isNotNull()

        // Clear error
        viewModel.clearError()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isNull()
    }

    // ── Edge Cases ─────────────────────────────────────────────

    @Test
    fun appendDigit_zero_isValid() = runTest {
        viewModel = PinLockViewModel(settingDao, context)
        advanceUntilIdle()

        viewModel.appendDigit(0)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.pin).isEqualTo("0")
    }

    @Test
    fun appendDigit_nine_isValid() = runTest {
        viewModel = PinLockViewModel(settingDao, context)
        advanceUntilIdle()

        viewModel.appendDigit(9)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.pin).isEqualTo("9")
    }

    @Test
    fun deleteDigit_worksOnConfirmPin() = runTest {
        viewModel = PinLockViewModel(settingDao, context)
        viewModel.setMode(PinLockMode.SETUP)
        advanceUntilIdle()

        // Enter PIN to move to CONFIRM step
        viewModel.appendDigit(1)
        viewModel.appendDigit(2)
        viewModel.appendDigit(3)
        viewModel.appendDigit(4)
        advanceUntilIdle()

        // Add digits to confirm PIN
        viewModel.appendDigit(1)
        viewModel.appendDigit(2)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.confirmPin).isEqualTo("12")

        // Delete from confirm PIN
        viewModel.deleteDigit()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.confirmPin).isEqualTo("1")
    }

    @Test
    fun loadPinStatus_isLoadedOnInit() = runTest {
        whenever(settingDao.getSettingValue("pin_enabled")).thenReturn("true")

        viewModel = PinLockViewModel(settingDao, context)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isPinEnabled).isTrue()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun isPinSet_returnsTrueWhenEnabled() = runTest {
        whenever(settingDao.getSetting("pin_enabled")).thenReturn(
            flowOf(Setting("pin_enabled", "true"))
        )

        viewModel = PinLockViewModel(settingDao, context)
        advanceUntilIdle()

        val isPinSet = viewModel.isPinSet().first()
        assertThat(isPinSet).isTrue()
    }

    @Test
    fun isPinSet_returnsFalseWhenDisabled() = runTest {
        whenever(settingDao.getSetting("pin_enabled")).thenReturn(
            flowOf(Setting("pin_enabled", "false"))
        )

        viewModel = PinLockViewModel(settingDao, context)
        advanceUntilIdle()

        val isPinSet = viewModel.isPinSet().first()
        assertThat(isPinSet).isFalse()
    }
}
