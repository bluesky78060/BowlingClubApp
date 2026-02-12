package com.bowlingclub.app.ui.score

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bowlingclub.app.data.local.entity.Member
import com.bowlingclub.app.data.local.entity.Tournament
import com.bowlingclub.app.data.local.entity.TournamentParticipant
import com.bowlingclub.app.ui.theme.BowlingClubTheme
import com.bowlingclub.app.ui.theme.BowlingTextStyles
import com.bowlingclub.app.viewmodel.ScoreInputUiState
import com.bowlingclub.app.viewmodel.ScoreInputViewModel
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualScoreInputScreen(
    modifier: Modifier = Modifier,
    viewModel: ScoreInputViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "점수 입력",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        ScoreInputContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onScoreChange = { memberId, gameNumber, score ->
                viewModel.updateScore(memberId, gameNumber, score)
            },
            onSaveClick = { viewModel.saveAllScores() }
        )
    }
}

@Composable
internal fun ScoreInputContent(
    modifier: Modifier = Modifier,
    uiState: ScoreInputUiState,
    onScoreChange: (Int, Int, Int) -> Unit,
    onSaveClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.fillMaxSize())
            }
            uiState.participants.isEmpty() -> {
                EmptyState(modifier = Modifier.fillMaxSize())
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TournamentHeader(
                        tournament = uiState.tournament,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 88.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.participants, key = { it.id }) { participant ->
                            ParticipantScoreCard(
                                participant = participant,
                                memberName = uiState.memberNames[participant.memberId] ?: "알 수 없음",
                                scores = uiState.scores[participant.memberId] ?: List(uiState.gameCount) { 0 },
                                gameCount = uiState.gameCount,
                                onScoreChange = { gameNumber, score ->
                                    onScoreChange(participant.memberId, gameNumber, score)
                                }
                            )
                        }
                    }

                    SaveButton(
                        onClick = onSaveClick,
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TournamentHeader(
    tournament: Tournament?,
    modifier: Modifier = Modifier
) {
    tournament?.let {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "게임 수: ${it.gameCount}게임",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    if (it.handicapEnabled) {
                        Text(
                            text = "핸디캡 적용",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantScoreCard(
    participant: TournamentParticipant,
    memberName: String,
    scores: List<Int>,
    gameCount: Int,
    onScoreChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = memberName,
                    style = BowlingTextStyles.MemberName,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val total = scores.sum()
                Text(
                    text = "합계: $total",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(gameCount) { index ->
                    ScoreTextField(
                        gameNumber = index + 1,
                        score = scores.getOrElse(index) { 0 },
                        onScoreChange = { newScore ->
                            onScoreChange(index + 1, newScore)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreTextField(
    gameNumber: Int,
    score: Int,
    onScoreChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "${gameNumber}G",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = if (score == 0) "" else score.toString(),
            onValueChange = { newValue ->
                val parsedScore = newValue.filter { it.isDigit() }.toIntOrNull() ?: 0
                if (parsedScore in 0..300) {
                    onScoreChange(parsedScore)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.titleLarge.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            placeholder = {
                Text(
                    text = "0",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        textAlign = TextAlign.Center
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
private fun SaveButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        if (enabled) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "저장하기",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "저장 중...",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "점수 정보를 불러오는 중...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🎳",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp)
            )
            Text(
                text = "참가자가 없습니다",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "먼저 대회에 참가자를 등록해주세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ManualScoreInputScreenPreview() {
    BowlingClubTheme {
        Surface {
            ScoreInputContent(
                uiState = ScoreInputUiState(
                    tournament = Tournament(
                        id = 1,
                        name = "2024년 2월 정기 대회",
                        date = LocalDate.of(2024, 2, 15),
                        gameCount = 3,
                        handicapEnabled = true,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    ),
                    participants = listOf(
                        TournamentParticipant(
                            id = 1,
                            tournamentId = 1,
                            memberId = 1,
                            joinedAt = LocalDateTime.now()
                        ),
                        TournamentParticipant(
                            id = 2,
                            tournamentId = 1,
                            memberId = 2,
                            joinedAt = LocalDateTime.now()
                        )
                    ),
                    memberNames = mapOf(
                        1 to "김철수",
                        2 to "이영희"
                    ),
                    scores = mapOf(
                        1 to listOf(150, 165, 180),
                        2 to listOf(200, 195, 210)
                    ),
                    gameCount = 3
                ),
                onScoreChange = { _, _, _ -> },
                onSaveClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ManualScoreInputLoadingPreview() {
    BowlingClubTheme {
        Surface {
            ScoreInputContent(
                uiState = ScoreInputUiState(isLoading = true),
                onScoreChange = { _, _, _ -> },
                onSaveClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ManualScoreInputEmptyPreview() {
    BowlingClubTheme {
        Surface {
            ScoreInputContent(
                uiState = ScoreInputUiState(
                    tournament = Tournament(
                        id = 1,
                        name = "2024년 2월 정기 대회",
                        date = LocalDate.of(2024, 2, 15),
                        gameCount = 3,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    ),
                    participants = emptyList()
                ),
                onScoreChange = { _, _, _ -> },
                onSaveClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticipantScoreCardPreview() {
    BowlingClubTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ParticipantScoreCard(
                participant = TournamentParticipant(
                    id = 1,
                    tournamentId = 1,
                    memberId = 1,
                    joinedAt = LocalDateTime.now()
                ),
                memberName = "김철수",
                scores = listOf(180, 195, 210),
                gameCount = 3,
                onScoreChange = { _, _ -> }
            )
        }
    }
}
