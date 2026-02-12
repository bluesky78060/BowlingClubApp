package com.bowlingclub.app.ui.score

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bowlingclub.app.ui.theme.BowlingClubTheme
import com.bowlingclub.app.viewmodel.OcrUiState

/**
 * OCR Preview Content Component
 *
 * Design Direction: Industrial Data Interface
 * - Brutalist card layout with sharp corners and strong borders
 * - Color-coded confidence system (traffic light metaphor)
 * - Data-dense table-like score presentation
 * - High-contrast interactive elements
 * - Monospace-inspired number fields
 *
 * This is a stateless content composable designed to be used within
 * a tab-based score input screen. All state and callbacks are provided
 * by the parent composable.
 */
@Composable
fun OcrPreviewContent(
    modifier: Modifier = Modifier,
    uiState: OcrUiState,
    onScoreChange: (playerName: String, gameIndex: Int, score: Int) -> Unit,
    onMemberMappingChange: (playerName: String, memberId: Int) -> Unit,
    onSaveClick: () -> Unit,
    onRetakeClick: () -> Unit
) {
    val canSave by remember(uiState.editableScores, uiState.memberMapping) {
        derivedStateOf {
            uiState.editableScores.isNotEmpty() &&
                    uiState.editableScores.keys.all { playerName ->
                        uiState.memberMapping.containsKey(playerName)
                    }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Confidence Indicator
            item {
                ConfidenceIndicator(
                    confidence = uiState.ocrConfidence,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Raw Text Debug Section
            item {
                RawTextSection(
                    rawText = uiState.parseResult?.rawText ?: "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            // Player Score Cards
            items(
                items = uiState.editableScores.entries.toList(),
                key = { it.key }
            ) { (playerName, scores) ->
                PlayerScoreCard(
                    playerName = playerName,
                    scores = scores,
                    gameCount = uiState.gameCount,
                    selectedMemberId = uiState.memberMapping[playerName],
                    availableMembers = uiState.availableMembers,
                    confidence = uiState.parseResult?.rows
                        ?.find { it.playerName == playerName }?.confidence ?: 0f,
                    onScoreChange = { gameIndex, score ->
                        onScoreChange(playerName, gameIndex, score)
                    },
                    onMemberSelect = { memberId ->
                        onMemberMappingChange(playerName, memberId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Action Buttons (Fixed at bottom)
        ActionButtonBar(
            canSave = canSave,
            onSaveClick = onSaveClick,
            onRetakeClick = onRetakeClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

/**
 * Confidence Indicator - Traffic light color-coded bar
 */
@Composable
private fun ConfidenceIndicator(
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val confidenceColor = when {
        confidence >= 0.8f -> Color(0xFF00C853) // Vibrant green
        confidence >= 0.5f -> Color(0xFFFFD600) // Electric yellow
        else -> Color(0xFFFF3D00) // Warning red
    }

    val confidenceLabel = when {
        confidence >= 0.8f -> "인식 품질: 우수"
        confidence >= 0.5f -> "인식 품질: 보통"
        else -> "인식 품질: 낮음 - 수동 확인 필요"
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = confidenceLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = confidenceColor
                )
            }

            // Progress bar with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(confidence)
                        .height(8.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    confidenceColor.copy(alpha = 0.7f),
                                    confidenceColor
                                ),
                                start = Offset.Zero,
                                end = Offset(Float.POSITIVE_INFINITY, 0f)
                            )
                        )
                )
            }
        }
    }
}

/**
 * Raw Text Debug Section - Collapsible
 */
@Composable
private fun RawTextSection(
    rawText: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "원본 OCR 텍스트",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (expanded) "접기" else "펼치기",
                    modifier = Modifier
                        .size(24.dp)
                        .alpha(if (expanded) 0.5f else 1f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF212121))
                        .padding(12.dp)
                ) {
                    Text(
                        text = rawText.ifBlank { "(텍스트 없음)" },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF00FF41), // Matrix green
                        fontWeight = FontWeight.Normal,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.5f
                    )
                }
            }
        }
    }
}

/**
 * Player Score Card - Main data entry component
 */
@Composable
private fun PlayerScoreCard(
    playerName: String,
    scores: List<Int>,
    gameCount: Int,
    selectedMemberId: Int?,
    availableMembers: Map<Int, String>,
    confidence: Float,
    onScoreChange: (gameIndex: Int, score: Int) -> Unit,
    onMemberSelect: (memberId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isMapped = selectedMemberId != null

    Card(
        modifier = modifier
            .shadow(
                elevation = if (isMapped) 4.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isMapped) Color.Transparent else Color(0xFFFF6F00),
                spotColor = if (isMapped) Color.Transparent else Color(0xFFFF6F00)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isMapped) 0.dp else 2.dp,
                    color = if (isMapped) Color.Transparent else Color(0xFFFF6F00),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Player Name + Confidence Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = playerName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                ConfidenceBadge(confidence = confidence)
            }

            // Member Mapping Dropdown
            MemberDropdown(
                selectedMemberId = selectedMemberId,
                availableMembers = availableMembers,
                onMemberSelect = onMemberSelect,
                modifier = Modifier.fillMaxWidth()
            )

            // Unmapped Warning
            if (!isMapped) {
                UnmappedWarning(modifier = Modifier.fillMaxWidth())
            }

            // Score Grid
            ScoreGrid(
                scores = scores,
                gameCount = gameCount,
                onScoreChange = onScoreChange,
                modifier = Modifier.fillMaxWidth()
            )

            // Total Score
            TotalScoreDisplay(
                total = scores.sum(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Confidence Badge - Small circular indicator
 */
@Composable
private fun ConfidenceBadge(
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val color = when {
        confidence >= 0.8f -> Color(0xFF00C853)
        confidence >= 0.5f -> Color(0xFFFFD600)
        else -> Color(0xFFFF3D00)
    }

    val icon: ImageVector = when {
        confidence >= 0.8f -> Icons.Default.Check
        confidence >= 0.5f -> Icons.Default.Warning
        else -> Icons.Default.Close
    }

    Box(
        modifier = modifier
            .size(32.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "신뢰도: ${(confidence * 100).toInt()}%",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Member Dropdown - Select registered member
 */
@Composable
private fun MemberDropdown(
    selectedMemberId: Int?,
    availableMembers: Map<Int, String>,
    onMemberSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = selectedMemberId?.let { availableMembers[it] } ?: "회원 선택"

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("등록 회원") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "드롭다운",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (selectedMemberId == null) {
                    Color(0xFFFF6F00)
                } else {
                    MaterialTheme.colorScheme.primary
                },
                unfocusedBorderColor = if (selectedMemberId == null) {
                    Color(0xFFFF6F00)
                } else {
                    MaterialTheme.colorScheme.outline
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            availableMembers.forEach { (memberId, memberName) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = memberName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    onClick = {
                        onMemberSelect(memberId)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Unmapped Warning - Alert when player not mapped to member
 */
@Composable
private fun UnmappedWarning(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFF6F00).copy(alpha = 0.1f))
            .border(1.dp, Color(0xFFFF6F00), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "경고",
            tint = Color(0xFFFF6F00),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "등록 회원을 선택해주세요",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFD84315)
        )
    }
}

/**
 * Score Grid - Editable score fields
 */
@Composable
private fun ScoreGrid(
    scores: List<Int>,
    gameCount: Int,
    onScoreChange: (gameIndex: Int, score: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "게임",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            repeat(gameCount) { index ->
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Score input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "점수",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            )
            repeat(gameCount) { index ->
                ScoreTextField(
                    score = scores.getOrElse(index) { 0 },
                    onScoreChange = { newScore ->
                        onScoreChange(index, newScore)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Score TextField - Single score input
 */
@Composable
private fun ScoreTextField(
    score: Int,
    onScoreChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var textValue by remember(score) { mutableStateOf(score.toString()) }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            textValue = newValue
            val parsed = newValue.toIntOrNull()
            if (parsed != null && parsed in 0..300) {
                onScoreChange(parsed)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        ),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        modifier = modifier.height(56.dp)
    )
}

/**
 * Total Score Display
 */
@Composable
private fun TotalScoreDisplay(
    total: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "합계",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = total.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Action Button Bar - Fixed bottom buttons
 */
@Composable
private fun ActionButtonBar(
    canSave: Boolean,
    onSaveClick: () -> Unit,
    onRetakeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetakeClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                        )
                    )
                )
            ) {
                Text(
                    text = "다시 촬영",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onSaveClick,
                enabled = canSave,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "저장하기",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================================================
// COMPOSE PREVIEWS
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun OcrPreviewContentPreview() {
    BowlingClubTheme {
        OcrPreviewContent(
            uiState = OcrUiState(
                isProcessing = false,
                capturedImageUri = null,
                parseResult = com.bowlingclub.app.data.remote.OcrParseResult(
                    rows = listOf(
                        com.bowlingclub.app.data.remote.ParsedScoreRow(
                            playerName = "홍길동",
                            scores = listOf(180, 200, 195),
                            confidence = 0.92f
                        ),
                        com.bowlingclub.app.data.remote.ParsedScoreRow(
                            playerName = "김철수",
                            scores = listOf(150, 165, 170),
                            confidence = 0.68f
                        ),
                        com.bowlingclub.app.data.remote.ParsedScoreRow(
                            playerName = "이영희",
                            scores = listOf(220, 210, 225),
                            confidence = 0.45f
                        )
                    ),
                    rawText = "홍길동 180 200 195\n김철수 150 165 170\n이영희 220 210 225",
                    isSuccess = true
                ),
                editableScores = mapOf(
                    "홍길동" to listOf(180, 200, 195),
                    "김철수" to listOf(150, 165, 170),
                    "이영희" to listOf(220, 210, 225)
                ),
                memberMapping = mapOf(
                    "홍길동" to 1,
                    "김철수" to 2
                ),
                availableMembers = mapOf(
                    1 to "홍길동",
                    2 to "김철수",
                    3 to "박영수",
                    4 to "이영희"
                ),
                tournamentId = 1,
                gameCount = 3,
                error = null,
                retryCount = 0,
                isSaved = false,
                ocrConfidence = 0.68f
            ),
            onScoreChange = { _, _, _ -> },
            onMemberMappingChange = { _, _ -> },
            onSaveClick = {},
            onRetakeClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfidenceIndicatorHighPreview() {
    BowlingClubTheme {
        ConfidenceIndicator(
            confidence = 0.92f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfidenceIndicatorLowPreview() {
    BowlingClubTheme {
        ConfidenceIndicator(
            confidence = 0.35f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayerScoreCardMappedPreview() {
    BowlingClubTheme {
        PlayerScoreCard(
            playerName = "홍길동",
            scores = listOf(180, 200, 195),
            gameCount = 3,
            selectedMemberId = 1,
            availableMembers = mapOf(1 to "홍길동", 2 to "김철수"),
            confidence = 0.92f,
            onScoreChange = { _, _ -> },
            onMemberSelect = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayerScoreCardUnmappedPreview() {
    BowlingClubTheme {
        PlayerScoreCard(
            playerName = "이영희",
            scores = listOf(220, 210, 225),
            gameCount = 3,
            selectedMemberId = null,
            availableMembers = mapOf(1 to "홍길동", 2 to "김철수", 3 to "이영희"),
            confidence = 0.45f,
            onScoreChange = { _, _ -> },
            onMemberSelect = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Dark Theme")
@Composable
private fun OcrPreviewContentDarkPreview() {
    BowlingClubTheme(darkTheme = true) {
        OcrPreviewContent(
            uiState = OcrUiState(
                isProcessing = false,
                capturedImageUri = null,
                parseResult = com.bowlingclub.app.data.remote.OcrParseResult(
                    rows = listOf(
                        com.bowlingclub.app.data.remote.ParsedScoreRow(
                            playerName = "홍길동",
                            scores = listOf(180, 200, 195),
                            confidence = 0.92f
                        )
                    ),
                    rawText = "홍길동 180 200 195",
                    isSuccess = true
                ),
                editableScores = mapOf("홍길동" to listOf(180, 200, 195)),
                memberMapping = mapOf("홍길동" to 1),
                availableMembers = mapOf(1 to "홍길동"),
                tournamentId = 1,
                gameCount = 3,
                error = null,
                retryCount = 0,
                isSaved = false,
                ocrConfidence = 0.92f
            ),
            onScoreChange = { _, _, _ -> },
            onMemberMappingChange = { _, _ -> },
            onSaveClick = {},
            onRetakeClick = {}
        )
    }
}
