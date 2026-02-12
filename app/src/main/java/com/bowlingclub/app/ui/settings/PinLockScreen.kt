package com.bowlingclub.app.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bowlingclub.app.ui.theme.BowlingClubTheme
import com.bowlingclub.app.viewmodel.PinLockMode
import com.bowlingclub.app.viewmodel.PinLockUiState
import com.bowlingclub.app.viewmodel.PinLockViewModel
import com.bowlingclub.app.viewmodel.PinSetupStep

@Composable
fun PinLockScreen(
    onNavigateBack: () -> Unit,
    onUnlocked: () -> Unit = {},
    mode: PinLockMode = PinLockMode.VERIFY,
    viewModel: PinLockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(mode) {
        viewModel.setMode(mode)
    }

    LaunchedEffect(uiState.isUnlocked) {
        if (uiState.isUnlocked) {
            onUnlocked()
            onNavigateBack()
        }
    }

    PinLockContent(
        uiState = uiState,
        onDigitClick = { digit -> viewModel.appendDigit(digit) },
        onDeleteClick = { viewModel.deleteDigit() },
        onCancelClick = onNavigateBack,
        onErrorDismiss = { viewModel.clearError() }
    )
}

@Composable
private fun PinLockContent(
    uiState: PinLockUiState,
    onDigitClick: (Int) -> Unit,
    onDeleteClick: () -> Unit,
    onCancelClick: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 상단 제목
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = getTitleText(uiState.mode, uiState.step, uiState.isOldPinVerified),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // PIN 표시 인디케이터
                PinIndicator(
                    pinLength = getCurrentPin(uiState).length,
                    maxLength = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 에러 메시지
                AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Text(
                        text = uiState.error ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // 중앙 숫자 키패드
            NumberKeypad(
                onDigitClick = onDigitClick,
                onDeleteClick = onDeleteClick
            )

            // 하단 취소 버튼
            if (uiState.mode in listOf(PinLockMode.SETUP, PinLockMode.CHANGE)) {
                TextButton(
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "취소",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun PinIndicator(
    pinLength: Int,
    maxLength: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(maxLength) { index ->
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .then(
                        if (index < pinLength) {
                            Modifier.background(MaterialTheme.colorScheme.primary)
                        } else {
                            Modifier.border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                        }
                    )
            )
        }
    }
}

@Composable
private fun NumberKeypad(
    onDigitClick: (Int) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1-3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            NumberKey(number = 1, onClick = { onDigitClick(1) })
            NumberKey(number = 2, onClick = { onDigitClick(2) })
            NumberKey(number = 3, onClick = { onDigitClick(3) })
        }

        // 4-6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            NumberKey(number = 4, onClick = { onDigitClick(4) })
            NumberKey(number = 5, onClick = { onDigitClick(5) })
            NumberKey(number = 6, onClick = { onDigitClick(6) })
        }

        // 7-9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            NumberKey(number = 7, onClick = { onDigitClick(7) })
            NumberKey(number = 8, onClick = { onDigitClick(8) })
            NumberKey(number = 9, onClick = { onDigitClick(9) })
        }

        // 빈칸, 0, 삭제
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            Spacer(modifier = Modifier.size(72.dp))
            NumberKey(number = 0, onClick = { onDigitClick(0) })
            DeleteKey(onClick = onDeleteClick)
        }
    }
}

@Composable
private fun NumberKey(
    number: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeleteKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(72.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "삭제",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getTitleText(mode: PinLockMode, step: PinSetupStep, isOldPinVerified: Boolean = false): String {
    return when (mode) {
        PinLockMode.VERIFY -> "PIN 입력"
        PinLockMode.SETUP -> {
            when (step) {
                PinSetupStep.ENTER -> "새 PIN 설정"
                PinSetupStep.CONFIRM -> "PIN 확인"
            }
        }
        PinLockMode.CHANGE -> {
            when {
                step == PinSetupStep.ENTER && !isOldPinVerified -> "현재 PIN 입력"
                step == PinSetupStep.ENTER && isOldPinVerified -> "새 PIN 설정"
                else -> "새 PIN 확인"
            }
        }
        PinLockMode.DISABLE -> "현재 PIN 입력"
    }
}

private fun getCurrentPin(uiState: PinLockUiState): String {
    return when (uiState.step) {
        PinSetupStep.ENTER -> uiState.pin
        PinSetupStep.CONFIRM -> uiState.confirmPin
    }
}

@Preview(showBackground = true)
@Composable
private fun PinLockScreenPreview() {
    BowlingClubTheme {
        PinLockContent(
            uiState = PinLockUiState(
                mode = PinLockMode.VERIFY,
                pin = "12"
            ),
            onDigitClick = {},
            onDeleteClick = {},
            onCancelClick = {},
            onErrorDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PinLockScreenErrorPreview() {
    BowlingClubTheme {
        PinLockContent(
            uiState = PinLockUiState(
                mode = PinLockMode.VERIFY,
                pin = "",
                error = "PIN이 일치하지 않습니다"
            ),
            onDigitClick = {},
            onDeleteClick = {},
            onCancelClick = {},
            onErrorDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PinLockScreenSetupPreview() {
    BowlingClubTheme {
        PinLockContent(
            uiState = PinLockUiState(
                mode = PinLockMode.SETUP,
                step = PinSetupStep.ENTER,
                pin = "123"
            ),
            onDigitClick = {},
            onDeleteClick = {},
            onCancelClick = {},
            onErrorDismiss = {}
        )
    }
}
