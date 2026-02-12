package com.bowlingclub.app.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bowlingclub.app.ui.theme.BowlingClubTheme
import com.bowlingclub.app.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToPinSetup: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.restoreFromUri(it) }
    }

    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importMembersCsv(it) }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "설정이 저장되었습니다",
                    duration = SnackbarDuration.Short
                )
            }
            viewModel.clearSavedState()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Long
                )
            }
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.backupMessage) {
        uiState.backupMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
            viewModel.clearBackupMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "설정",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ClubSettingsSection(
                clubName = uiState.clubName,
                onClubNameChange = viewModel::updateClubName,
                onSaveClubName = viewModel::saveClubName
            )

            AppearanceSection()

            // ── 새 섹션들 ──────────────────────────────

            BackupSection(
                lastBackupTime = uiState.lastBackupTime,
                isBackupInProgress = uiState.isBackupInProgress,
                isRestoreInProgress = uiState.isRestoreInProgress,
                onBackupClick = viewModel::createBackup,
                onRestoreClick = { restoreLauncher.launch(arrayOf("application/json")) },
                onShareClick = viewModel::shareBackup,
                onExportScoresCsvClick = viewModel::exportScoresCsv,
                onExportMembersCsvClick = viewModel::exportMembersCsv,
                onImportMembersCsvClick = {
                    csvImportLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
                }
            )

            AutoBackupSection(
                isAutoBackupEnabled = uiState.isAutoBackupEnabled,
                onToggle = viewModel::toggleAutoBackup
            )

            PinLockSection(
                isPinEnabled = uiState.isPinEnabled,
                onPinSetup = { onNavigateToPinSetup("SETUP") },
                onPinChange = { onNavigateToPinSetup("CHANGE") },
                onPinDisable = { onNavigateToPinSetup("DISABLE") }
            )

            AboutSection()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── 섹션들 ───────────────────────────────────────

@Composable
private fun ClubSettingsSection(
    clubName: String,
    onClubNameChange: (String) -> Unit,
    onSaveClubName: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsCard(
        title = "클럽 설정",
        icon = Icons.Default.Groups,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = clubName,
                onValueChange = onClubNameChange,
                label = { Text("클럽 명칭") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Button(
                onClick = onSaveClubName,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "저장",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun AppearanceSection(modifier: Modifier = Modifier) {
    SettingsCard(
        title = "테마",
        icon = Icons.Default.Palette,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "다크 모드",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "시스템 설정 따르기",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = false,
                onCheckedChange = {},
                enabled = false
            )
        }
    }
}

@Composable
private fun BackupSection(
    lastBackupTime: String?,
    isBackupInProgress: Boolean,
    isRestoreInProgress: Boolean,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onShareClick: () -> Unit,
    onExportScoresCsvClick: () -> Unit,
    onExportMembersCsvClick: () -> Unit,
    onImportMembersCsvClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsCard(
        title = "백업 / 복원",
        icon = Icons.Default.Refresh,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 마지막 백업 시간
            Text(
                text = if (lastBackupTime != null) {
                    "마지막 백업: $lastBackupTime"
                } else {
                    "백업 기록 없음"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 수동 백업 버튼
            Button(
                onClick = onBackupClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBackupInProgress && !isRestoreInProgress,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isBackupInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isBackupInProgress) "백업 중..." else "수동 백업",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // 복원 버튼
            OutlinedButton(
                onClick = onRestoreClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBackupInProgress && !isRestoreInProgress,
                shape = MaterialTheme.shapes.medium
            ) {
                if (isRestoreInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isRestoreInProgress) "복원 중..." else "복원",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // 백업 공유
            OutlinedButton(
                onClick = onShareClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBackupInProgress && !isRestoreInProgress,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "백업 공유",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // 점수 CSV 내보내기
            OutlinedButton(
                onClick = onExportScoresCsvClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBackupInProgress && !isRestoreInProgress,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "점수 CSV 내보내기",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // 회원 CSV 내보내기
            OutlinedButton(
                onClick = onExportMembersCsvClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBackupInProgress && !isRestoreInProgress,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "회원 CSV 내보내기",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // 회원 CSV 가져오기
            Button(
                onClick = onImportMembersCsvClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBackupInProgress && !isRestoreInProgress,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text(
                    text = "회원 CSV 가져오기",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun AutoBackupSection(
    isAutoBackupEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsCard(
        title = "자동 백업",
        icon = Icons.Default.DateRange,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isAutoBackupEnabled) "자동 백업 활성화됨" else "자동 백업 비활성화됨",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "매주 자동으로 백업합니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = isAutoBackupEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun PinLockSection(
    isPinEnabled: Boolean,
    onPinSetup: () -> Unit,
    onPinChange: () -> Unit,
    onPinDisable: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsCard(
        title = "앱 잠금",
        icon = Icons.Default.Lock,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isPinEnabled) "PIN 잠금이 설정되어 있습니다" else "PIN 잠금이 설정되지 않았습니다",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isPinEnabled) {
                Button(
                    onClick = onPinSetup,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "PIN 설정",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPinChange,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "PIN 변경",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    TextButton(
                        onClick = onPinDisable,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "PIN 해제",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutSection(modifier: Modifier = Modifier) {
    SettingsCard(
        title = "앱 정보",
        icon = Icons.Default.Info,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AboutRow(label = "앱 버전", value = "1.0.0")
            AboutRow(label = "개발자", value = "BowlingClub Team")
            AboutRow(label = "기술 스택", value = "Kotlin + Jetpack Compose")
        }
    }
}

@Composable
private fun AboutRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── 공통 컴포넌트 ────────────────────────────────────────────

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            content()
        }
    }
}

// ── Preview ──────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    BowlingClubTheme {
        Surface {
            SettingsScreen()
        }
    }
}

