package com.bowlingclub.app.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowlingclub.app.data.local.dao.SettingDao
import com.bowlingclub.app.data.local.entity.Setting
import com.bowlingclub.app.util.BackupManager
import com.bowlingclub.app.util.CsvExporter
import com.bowlingclub.app.work.AutoBackupWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class SettingsUiState(
    val clubName: String = "볼링클럽",
    val isSaved: Boolean = false,
    val error: String? = null,
    val isLoading: Boolean = false,
    val lastBackupTime: String? = null,
    val isAutoBackupEnabled: Boolean = false,
    val isPinEnabled: Boolean = false,
    val backupMessage: String? = null,
    val isBackupInProgress: Boolean = false,
    val isRestoreInProgress: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingDao: SettingDao,
    private val backupManager: BackupManager,
    private val csvExporter: CsvExporter,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    companion object {
        const val KEY_CLUB_NAME = "club_name"
        private const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
        private const val KEY_PIN_ENABLED = "pin_enabled"
        private const val KEY_LAST_BACKUP_TIME = "last_backup_time"
    }

    private val backupTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

    init {
        loadClubName()
        loadLastBackupTime()
        loadAutoBackupStatus()
        loadPinStatus()
    }


    // ── 클럽 설정 ────────────────────────────────────────────

    private fun loadClubName() {
        viewModelScope.launch {
            val name = settingDao.getSettingValue(KEY_CLUB_NAME) ?: "볼링클럽"
            _uiState.update { it.copy(clubName = name) }
        }
    }

    fun updateClubName(name: String) {
        _uiState.update { it.copy(clubName = name) }
    }

    fun saveClubName() {
        viewModelScope.launch {
            val name = _uiState.value.clubName.trim().ifBlank { "볼링클럽" }
            settingDao.insertOrUpdate(Setting(KEY_CLUB_NAME, name))
            _uiState.update { it.copy(clubName = name, isSaved = true) }
        }
    }

    // ── 백업/복원 ────────────────────────────────────────────

    fun createBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupInProgress = true) }
            backupManager.createBackup()
                .onSuccess {
                    loadLastBackupTime()
                    _uiState.update {
                        it.copy(
                            isBackupInProgress = false,
                            backupMessage = "백업이 완료되었습니다"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isBackupInProgress = false,
                            backupMessage = "백업 실패: ${e.message}"
                        )
                    }
                }
        }
    }

    fun restoreFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoreInProgress = true) }
            backupManager.restoreFromUri(uri)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isRestoreInProgress = false,
                            backupMessage = "복원 완료: 회원 ${result.membersRestored}명, " +
                                "대회 ${result.tournamentsRestored}건, " +
                                "점수 ${result.scoresRestored}건"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isRestoreInProgress = false,
                            backupMessage = "복원 실패: ${e.message}"
                        )
                    }
                }
        }
    }

    fun shareBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupInProgress = true) }
            backupManager.shareBackup()
                .onSuccess {
                    loadLastBackupTime()
                    _uiState.update {
                        it.copy(
                            isBackupInProgress = false,
                            backupMessage = "백업 파일 공유가 시작되었습니다"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isBackupInProgress = false,
                            backupMessage = "백업 공유 실패: ${e.message}"
                        )
                    }
                }
        }
    }

    // ── CSV 내보내기 ────────────────────────────────────────

    fun exportScoresCsv() {
        viewModelScope.launch {
            csvExporter.exportAllScores()
                .onSuccess { uri ->
                    shareCsvFile(uri, "점수 데이터 CSV")
                    _uiState.update { it.copy(backupMessage = "점수 CSV 내보내기가 시작되었습니다") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(backupMessage = "점수 CSV 내보내기 실패: ${e.message}") }
                }
        }
    }

    fun exportMembersCsv() {
        viewModelScope.launch {
            csvExporter.exportMembers()
                .onSuccess { uri ->
                    shareCsvFile(uri, "회원 데이터 CSV")
                    _uiState.update { it.copy(backupMessage = "회원 CSV 내보내기가 시작되었습니다") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(backupMessage = "회원 CSV 내보내기 실패: ${e.message}") }
                }
        }
    }

    private fun shareCsvFile(uri: Uri, title: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    // ── 자동 백업 ──────────────────────────────────────────

    fun toggleAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            try {
                if (enabled) {
                    AutoBackupWorker.schedule(context)
                } else {
                    AutoBackupWorker.cancel(context)
                }
                settingDao.insertOrUpdate(Setting(KEY_AUTO_BACKUP_ENABLED, enabled.toString()))
                _uiState.update { it.copy(isAutoBackupEnabled = enabled) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(backupMessage = "자동 백업 설정 실패: ${e.message}")
                }
            }
        }
    }

    // ── PIN 잠금 ───────────────────────────────────────────

    fun loadPinStatus() {
        viewModelScope.launch {
            val pinEnabled = settingDao.getSettingValue(KEY_PIN_ENABLED)?.toBoolean() ?: false
            _uiState.update { it.copy(isPinEnabled = pinEnabled) }
        }
    }

    // ── 로드 헬퍼 ──────────────────────────────────────────

    private fun loadLastBackupTime() {
        viewModelScope.launch {
            val lastBackup = backupManager.getLastBackupTime()
            _uiState.update {
                it.copy(lastBackupTime = lastBackup?.format(backupTimeFormatter))
            }
        }
    }

    private fun loadAutoBackupStatus() {
        viewModelScope.launch {
            val enabled = settingDao.getSettingValue(KEY_AUTO_BACKUP_ENABLED)?.toBoolean() ?: false
            _uiState.update { it.copy(isAutoBackupEnabled = enabled) }
        }
    }

    // ── 상태 클리어 ────────────────────────────────────────

    fun clearBackupMessage() {
        _uiState.update { it.copy(backupMessage = null) }
    }

    fun clearSavedState() {
        _uiState.update { it.copy(isSaved = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
