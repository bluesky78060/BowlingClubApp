package com.bowlingclub.app.ui.team

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.graphics.Bitmap
import androidx.compose.ui.platform.LocalContext
import com.bowlingclub.app.data.local.entity.Team
import com.bowlingclub.app.ui.components.SharePreviewDialog
import com.bowlingclub.app.util.TeamRankingImageGenerator
import com.bowlingclub.app.util.ShareUtil
import com.bowlingclub.app.data.model.RankingResult
import com.bowlingclub.app.ui.theme.BowlingClubTheme
import com.bowlingclub.app.ui.theme.BronzeRank
import com.bowlingclub.app.ui.theme.GoldRank
import com.bowlingclub.app.ui.theme.SilverRank
import com.bowlingclub.app.util.TeamRankingResult
import com.bowlingclub.app.viewmodel.TeamResultUiState
import com.bowlingclub.app.viewmodel.TeamResultViewModel
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamResultScreen(
    modifier: Modifier = Modifier,
    viewModel: TeamResultViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showShareDialog by remember { mutableStateOf(false) }
    var shareBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("팀전 결과") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                actions = {
                    if (uiState.teamRankings.isNotEmpty()) {
                        IconButton(onClick = {
                            val bitmap = TeamRankingImageGenerator.generateTeamRankingBitmap(
                                tournamentName = uiState.tournamentName,
                                tournamentDate = "",
                                teamRankings = uiState.teamRankings,
                                handicapEnabled = uiState.handicapEnabled,
                                gameCount = uiState.gameCount
                            )
                            shareBitmap = bitmap
                            showShareDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "공유",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        TeamResultContent(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues)
        )
    }

    if (showShareDialog) {
        SharePreviewDialog(
            bitmap = shareBitmap,
            onShareImage = {
                showShareDialog = false
                shareBitmap?.recycle()
                shareBitmap = null
                val imageUri = TeamRankingImageGenerator.generateTeamRankingImage(
                    context = context,
                    tournamentName = uiState.tournamentName,
                    tournamentDate = "",
                    teamRankings = uiState.teamRankings,
                    handicapEnabled = uiState.handicapEnabled,
                    gameCount = uiState.gameCount
                )
                imageUri?.let { ShareUtil.shareImage(context, it, "${uiState.tournamentName} 팀전 결과") }
            },
            onShareText = {
                showShareDialog = false
                shareBitmap?.recycle()
                shareBitmap = null
                val text = ShareUtil.formatTeamRankingText(
                    tournamentName = uiState.tournamentName,
                    tournamentDate = "",
                    teamRankings = uiState.teamRankings,
                    handicapEnabled = uiState.handicapEnabled
                )
                ShareUtil.shareText(context, text, "${uiState.tournamentName} 팀전 결과")
            },
            onDismiss = {
                showShareDialog = false
                shareBitmap?.recycle()
                shareBitmap = null
            }
        )
    }
}

@Composable
private fun TeamResultContent(
    uiState: TeamResultUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
            uiState.teamRankings.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "팀 결과가 없습니다",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tournament title
                    item {
                        Text(
                            text = uiState.tournamentName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Summary section
                    item {
                        TeamSummaryCard(
                            teamRankings = uiState.teamRankings,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Team ranking cards
                    items(uiState.teamRankings) { teamRanking ->
                        TeamRankingCard(
                            teamRanking = teamRanking,
                            handicapEnabled = uiState.handicapEnabled,
                            gameCount = uiState.gameCount,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamSummaryCard(
    teamRankings: List<TeamRankingResult>,
    modifier: Modifier = Modifier
) {
    val mvp = teamRankings
        .flatMap { it.memberRankings }
        .maxByOrNull { it.finalTotal }

    val topTeam = teamRankings.firstOrNull()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "대회 요약",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            mvp?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "MVP",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${it.memberName} (${it.finalTotal}점)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            topTeam?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "우승팀",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${it.team.name} (${it.teamFinalTotal}점)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamRankingCard(
    teamRanking: TeamRankingResult,
    handicapEnabled: Boolean,
    gameCount: Int,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "expand_icon_rotation"
    )

    val rankColor = when (teamRanking.rank) {
        1 -> GoldRank
        2 -> SilverRank
        3 -> BronzeRank
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Team Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rankColor.copy(alpha = 0.2f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank medal
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(rankColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (teamRanking.rank <= 3) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "순위 메달",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Text(
                            text = "${teamRanking.rank}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Team name and score
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = teamRanking.team.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${teamRanking.teamFinalTotal}점",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Expand icon
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "접기" else "펼치기",
                    modifier = Modifier.rotate(rotationAngle)
                )
            }

            // Expanded content
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Team statistics
                    TeamStatsSection(
                        teamRanking = teamRanking,
                        handicapEnabled = handicapEnabled
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Member scores header
                    Text(
                        text = "팀원 점수",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Member scores table
                    MemberScoresTable(
                        memberRankings = teamRanking.memberRankings,
                        gameCount = gameCount,
                        handicapEnabled = handicapEnabled
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamStatsSection(
    teamRanking: TeamRankingResult,
    handicapEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "팀 통계",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatRow(label = "팀 합계", value = "${teamRanking.teamTotalScore}점")

        if (handicapEnabled) {
            StatRow(
                label = "핸디캡 합계",
                value = "${teamRanking.teamHandicapTotal}점"
            )
            StatRow(
                label = "최종 합계",
                value = "${teamRanking.teamFinalTotal}점",
                highlight = true
            )
        }

        StatRow(label = "팀 하이게임", value = "${teamRanking.teamHighGame}점")
        StatRow(
            label = "팀 평균",
            value = String.format("%.1f점", teamRanking.teamAverage)
        )
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MemberScoresTable(
    memberRankings: List<RankingResult>,
    gameCount: Int,
    handicapEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Table header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "이름",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.5f)
            )

            for (i in 1..gameCount) {
                Text(
                    text = "${i}G",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "합계",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }

        // Table rows
        memberRankings.forEach { member ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = member.memberName,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1.5f)
                )

                val scoreToDisplay = member.gameScores.take(gameCount)
                scoreToDisplay.forEach { score ->
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fill empty cells if needed
                repeat(gameCount - scoreToDisplay.size) {
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "${member.finalTotal}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (member != memberRankings.last()) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeamResultScreenPreview() {
    BowlingClubTheme {
        val sampleTeamRankings = listOf(
            TeamRankingResult(
                rank = 1,
                team = Team(
                    id = 1,
                    tournamentId = 1,
                    name = "드래곤팀",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                memberRankings = listOf(
                    RankingResult(
                        rank = 1,
                        memberId = 1,
                        memberName = "김철수",
                        gameScores = listOf(180, 200, 195),
                        totalScore = 575,
                        handicapTotal = 30,
                        finalTotal = 605,
                        highGame = 200,
                        average = 191.7
                    ),
                    RankingResult(
                        rank = 2,
                        memberId = 2,
                        memberName = "이영희",
                        gameScores = listOf(170, 185, 175),
                        totalScore = 530,
                        handicapTotal = 45,
                        finalTotal = 575,
                        highGame = 185,
                        average = 176.7
                    )
                ),
                teamTotalScore = 1105,
                teamHandicapTotal = 75,
                teamFinalTotal = 1180,
                teamHighGame = 200,
                teamAverage = 184.2
            ),
            TeamRankingResult(
                rank = 2,
                team = Team(
                    id = 2,
                    tournamentId = 1,
                    name = "타이거팀",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                memberRankings = listOf(
                    RankingResult(
                        rank = 1,
                        memberId = 3,
                        memberName = "박민수",
                        gameScores = listOf(165, 190, 180),
                        totalScore = 535,
                        handicapTotal = 40,
                        finalTotal = 575,
                        highGame = 190,
                        average = 178.3
                    )
                ),
                teamTotalScore = 535,
                teamHandicapTotal = 40,
                teamFinalTotal = 575,
                teamHighGame = 190,
                teamAverage = 178.3
            )
        )

        val sampleUiState = TeamResultUiState(
            tournamentName = "2024 봄 정기 대회",
            teamRankings = sampleTeamRankings,
            handicapEnabled = true,
            gameCount = 3,
            isLoading = false
        )

        TeamResultContent(uiState = sampleUiState)
    }
}

@Preview(showBackground = true)
@Composable
fun TeamRankingCardPreview() {
    BowlingClubTheme {
        val sampleTeamRanking = TeamRankingResult(
            rank = 1,
            team = Team(
                id = 1,
                tournamentId = 1,
                name = "드래곤팀",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            memberRankings = listOf(
                RankingResult(
                    rank = 1,
                    memberId = 1,
                    memberName = "김철수",
                    gameScores = listOf(180, 200, 195),
                    totalScore = 575,
                    handicapTotal = 30,
                    finalTotal = 605,
                    highGame = 200,
                    average = 191.7
                )
            ),
            teamTotalScore = 575,
            teamHandicapTotal = 30,
            teamFinalTotal = 605,
            teamHighGame = 200,
            teamAverage = 191.7
        )

        Surface {
            TeamRankingCard(
                teamRanking = sampleTeamRanking,
                handicapEnabled = true,
                gameCount = 3,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
