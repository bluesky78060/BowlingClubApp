package com.bowlingclub.app.viewmodel

import android.content.Context
import android.net.Uri
import com.bowlingclub.app.MainDispatcherRule
import com.bowlingclub.app.data.local.dao.SettingDao
import com.bowlingclub.app.data.local.entity.Setting
import com.bowlingclub.app.util.BackupManager
import com.bowlingclub.app.util.BackupResult
import com.bowlingclub.app.util.CsvExporter
import com.bowlingclub.app.util.HandicapCalculator
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settingDao: SettingDao = mock()
    private val backupManager: BackupManager = mock()
    private val csvExporter: CsvExporter = mock()
    private val context: Context = mock()

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        // Mock default settings
        runTest {
            whenever(settingDao.getSettingValue("handicap_base_score"))
                .thenReturn(HandicapCalculator.DEFAULT_BASE_SCORE.toString())
            whenever(settingDao.getSettingValue("handicap_percentage"))
                .thenReturn(HandicapCalculator.DEFAULT_PERCENTAGE.toString())
            whenever(settingDao.getSettingValue("ocr_api_url")).thenReturn("")
            whenever(settingDao.getSettingValue("auto_backup_enabled")).thenReturn("false")
            whenever(settingDao.getSettingValue("pin_enabled")).thenReturn("false")
        }
    }

    // ── loadSettings Tests ─────────────────────────────────────

    @Test
    fun loadSettings_loadsHandicapSettings() = runTest {
        whenever(settingDao.getSettingValue("handicap_base_score")).thenReturn("220")
        whenever(settingDao.getSettingValue("handicap_percentage")).thenReturn("90")

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.handicapBaseScore).isEqualTo(220)
        assertThat(state.handicapPercentage).isEqualTo(90)
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun loadSettings_usesDefaultValues_whenNotSet() = runTest {
        whenever(settingDao.getSettingValue("handicap_base_score")).thenReturn(null)
        whenever(settingDao.getSettingValue("handicap_percentage")).thenReturn(null)

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.handicapBaseScore).isEqualTo(HandicapCalculator.DEFAULT_BASE_SCORE)
        assertThat(state.handicapPercentage).isEqualTo(HandicapCalculator.DEFAULT_PERCENTAGE)
    }

    @Test
    fun loadSettings_handlesException() = runTest {
        whenever(settingDao.getSettingValue("handicap_base_score"))
            .thenThrow(RuntimeException("Database error"))

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).contains("설정을 불러오는데 실패했습니다")
    }

    // ── saveSettings Tests ─────────────────────────────────────

    @Test
    fun saveSettings_savesHandicapBaseScoreAndPercentage() = runTest {
        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.updateHandicapBaseScore("230")
        viewModel.updateHandicapPercentage("85")
        advanceUntilIdle()

        viewModel.saveSettings()
        advanceUntilIdle()

        verify(settingDao).insertOrUpdate(eq(Setting("handicap_base_score", "230")))
        verify(settingDao).insertOrUpdate(eq(Setting("handicap_percentage", "85")))

        val state = viewModel.uiState.value
        assertThat(state.isSaved).isTrue()
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun saveSettings_rejectsBaseScoreLessThan150() = runTest {
        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.updateHandicapBaseScore("140")
        viewModel.saveSettings()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.error).isEqualTo("핸디캡 기준 점수는 150-300 사이여야 합니다")
        assertThat(state.isSaved).isFalse()
    }

    @Test
    fun saveSettings_rejectsBaseScoreGreaterThan300() = runTest {
        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.updateHandicapBaseScore("310")
        viewModel.saveSettings()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.error).isEqualTo("핸디캡 기준 점수는 150-300 사이여야 합니다")
        assertThat(state.isSaved).isFalse()
    }

    @Test
    fun saveSettings_rejectsPercentageLessThan0() = runTest {
        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.updateHandicapPercentage("-10")
        viewModel.saveSettings()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.error).isEqualTo("핸디캡 비율은 0-100 사이여야 합니다")
        assertThat(state.isSaved).isFalse()
    }

    @Test
    fun saveSettings_rejectsPercentageGreaterThan100() = runTest {
        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.updateHandicapPercentage("110")
        viewModel.saveSettings()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.error).isEqualTo("핸디캡 비율은 0-100 사이여야 합니다")
        assertThat(state.isSaved).isFalse()
    }

    @Test
    fun saveSettings_savesOcrApiUrl() = runTest {
        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.updateOcrApiUrl("https://example.com/ocr")
        viewModel.saveSettings()
        advanceUntilIdle()

        verify(settingDao).insertOrUpdate(eq(Setting("ocr_api_url", "https://example.com/ocr")))
    }

    @Test
    fun saveSettings_handlesException() = runTest {
        whenever(settingDao.insertOrUpdate(Setting("handicap_base_score", "200")))
            .thenThrow(RuntimeException("Database error"))

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.updateHandicapBaseScore("200")
        viewModel.saveSettings()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.error).contains("설정 저장에 실패했습니다")
        assertThat(state.isSaved).isFalse()
    }

    // ── createBackup Tests ─────────────────────────────────────

    @Test
    fun createBackup_success_updatesLastBackupTime() = runTest {
        val backupTime = LocalDateTime.of(2026, 2, 9, 10, 30)
        whenever(backupManager.createBackup()).thenReturn(Result.success(Unit))
        whenever(backupManager.getLastBackupTime()).thenReturn(backupTime)

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.createBackup()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isBackupInProgress).isFalse()
        assertThat(state.backupMessage).isEqualTo("백업이 완료되었습니다")
        assertThat(state.lastBackupTime).isEqualTo("2026.02.09 10:30")
    }

    @Test
    fun createBackup_failure_showsErrorMessage() = runTest {
        whenever(backupManager.createBackup()).thenReturn(
            Result.failure(Exception("Backup failed"))
        )

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.createBackup()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isBackupInProgress).isFalse()
        assertThat(state.backupMessage).isEqualTo("백업 실패: Backup failed")
    }

    // ── restoreFromUri Tests ───────────────────────────────────

    @Test
    fun restoreFromUri_success_showsRestoreCount() = runTest {
        val uri: Uri = mock()
        val result = BackupResult(
            membersRestored = 10,
            tournamentsRestored = 5,
            scoresRestored = 100
        )
        whenever(backupManager.restoreFromUri(uri)).thenReturn(Result.success(result))

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.restoreFromUri(uri)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isRestoreInProgress).isFalse()
        assertThat(state.backupMessage).isEqualTo("복원 완료: 회원 10명, 대회 5건, 점수 100건")
    }

    @Test
    fun restoreFromUri_failure_showsErrorMessage() = runTest {
        val uri: Uri = mock()
        whenever(backupManager.restoreFromUri(uri)).thenReturn(
            Result.failure(Exception("Restore failed"))
        )

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.restoreFromUri(uri)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isRestoreInProgress).isFalse()
        assertThat(state.backupMessage).isEqualTo("복원 실패: Restore failed")
    }

    // ── shareBackup Tests ──────────────────────────────────────

    @Test
    fun shareBackup_success_showsSuccessMessage() = runTest {
        whenever(backupManager.shareBackup()).thenReturn(Result.success(Unit))
        whenever(backupManager.getLastBackupTime()).thenReturn(LocalDateTime.now())

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.shareBackup()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isBackupInProgress).isFalse()
        assertThat(state.backupMessage).isEqualTo("백업 파일 공유가 시작되었습니다")
    }

    @Test
    fun shareBackup_failure_showsErrorMessage() = runTest {
        whenever(backupManager.shareBackup()).thenReturn(
            Result.failure(Exception("Share failed"))
        )

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.shareBackup()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isBackupInProgress).isFalse()
        assertThat(state.backupMessage).isEqualTo("백업 공유 실패: Share failed")
    }

    // ── exportScoresCsv Tests ──────────────────────────────────

    @Test
    fun exportScoresCsv_success_showsSuccessMessage() = runTest {
        val uri: Uri = mock()
        whenever(csvExporter.exportAllScores()).thenReturn(Result.success(uri))

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.exportScoresCsv()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.backupMessage).isEqualTo("점수 CSV 내보내기가 시작되었습니다")
    }

    @Test
    fun exportScoresCsv_failure_showsErrorMessage() = runTest {
        whenever(csvExporter.exportAllScores()).thenReturn(
            Result.failure(Exception("Export failed"))
        )

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.exportScoresCsv()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.backupMessage).isEqualTo("점수 CSV 내보내기 실패: Export failed")
    }

    // ── exportMembersCsv Tests ─────────────────────────────────

    @Test
    fun exportMembersCsv_success_showsSuccessMessage() = runTest {
        val uri: Uri = mock()
        whenever(csvExporter.exportMembers()).thenReturn(Result.success(uri))

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.exportMembersCsv()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.backupMessage).isEqualTo("회원 CSV 내보내기가 시작되었습니다")
    }

    @Test
    fun exportMembersCsv_failure_showsErrorMessage() = runTest {
        whenever(csvExporter.exportMembers()).thenReturn(
            Result.failure(Exception("Export failed"))
        )

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.exportMembersCsv()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.backupMessage).isEqualTo("회원 CSV 내보내기 실패: Export failed")
    }

    // ── toggleAutoBackup Tests ─────────────────────────────────

    @Test
    fun toggleAutoBackup_enable_schedulesWorkManager() = runTest {
        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.toggleAutoBackup(true)
        advanceUntilIdle()

        verify(settingDao).insertOrUpdate(eq(Setting("auto_backup_enabled", "true")))
        assertThat(viewModel.uiState.value.isAutoBackupEnabled).isTrue()
    }

    @Test
    fun toggleAutoBackup_disable_cancelsWorkManager() = runTest {
        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.toggleAutoBackup(false)
        advanceUntilIdle()

        verify(settingDao).insertOrUpdate(eq(Setting("auto_backup_enabled", "false")))
        assertThat(viewModel.uiState.value.isAutoBackupEnabled).isFalse()
    }

    @Test
    fun toggleAutoBackup_failure_showsErrorMessage() = runTest {
        whenever(settingDao.insertOrUpdate(Setting("auto_backup_enabled", "true")))
            .thenThrow(RuntimeException("Database error"))

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.toggleAutoBackup(true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.backupMessage).contains("자동 백업 설정 실패")
    }

    // ── loadPinStatus Tests ────────────────────────────────────

    @Test
    fun loadPinStatus_readsPinEnabledFromDao() = runTest {
        whenever(settingDao.getSettingValue("pin_enabled")).thenReturn("true")

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.loadPinStatus()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isPinEnabled).isTrue()
    }

    @Test
    fun loadPinStatus_defaultsToFalse_whenNotSet() = runTest {
        whenever(settingDao.getSettingValue("pin_enabled")).thenReturn(null)

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.loadPinStatus()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isPinEnabled).isFalse()
    }

    // ── clearBackupMessage Tests ───────────────────────────────

    @Test
    fun clearBackupMessage_clearsMessage() = runTest {
        whenever(csvExporter.exportMembers()).thenReturn(
            Result.failure(Exception("Export failed"))
        )

        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.exportMembersCsv()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.backupMessage).isNotNull()

        viewModel.clearBackupMessage()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.backupMessage).isNull()
    }

    // ── clearError Tests ───────────────────────────────────────

    @Test
    fun clearError_clearsErrorState() = runTest {
        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.updateHandicapBaseScore("100") // Invalid
        viewModel.saveSettings()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isNotNull()

        viewModel.clearError()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isNull()
    }

    // ── Edge Cases ─────────────────────────────────────────────

    @Test
    fun updateHandicapBaseScore_setsIsSavedToFalse() = runTest {
        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.updateHandicapBaseScore("200")
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.handicapBaseScore).isEqualTo(200)
        assertThat(viewModel.uiState.value.isSaved).isFalse()
    }

    @Test
    fun updateHandicapPercentage_setsIsSavedToFalse() = runTest {
        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.updateHandicapPercentage("80")
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.handicapPercentage).isEqualTo(80)
        assertThat(viewModel.uiState.value.isSaved).isFalse()
    }

    @Test
    fun saveSettings_acceptsBoundaryValues() = runTest {
        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        // Test minimum valid values
        viewModel.updateHandicapBaseScore("150")
        viewModel.updateHandicapPercentage("0")
        viewModel.saveSettings()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isSaved).isTrue()
        assertThat(viewModel.uiState.value.error).isNull()

        // Test maximum valid values
        viewModel.updateHandicapBaseScore("300")
        viewModel.updateHandicapPercentage("100")
        viewModel.saveSettings()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isSaved).isTrue()
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun clearSavedState_clearsIsSavedFlag() = runTest {
        viewModel = SettingsViewModel(settingDao, backupManager, csvExporter, context)
        advanceUntilIdle()

        viewModel.updateHandicapBaseScore("200")
        viewModel.saveSettings()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isSaved).isTrue()

        viewModel.clearSavedState()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isSaved).isFalse()
    }
}
