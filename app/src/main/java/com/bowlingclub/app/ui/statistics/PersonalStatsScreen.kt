package com.bowlingclub.app.ui.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MenuAnchorType
import com.bowlingclub.app.data.local.entity.GameScore
import com.bowlingclub.app.data.model.PersonalStats
import com.bowlingclub.app.data.model.ScoreDistributionItem
import com.bowlingclub.app.data.model.ScoreTrendItem
import com.bowlingclub.app.ui.components.ScoreDistributionChart
import com.bowlingclub.app.ui.components.ScoreTrendChart
import com.bowlingclub.app.viewmodel.MemberSummary
import com.bowlingclub.app.viewmodel.PersonalStatsUiState
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalStatsContent(
    personalState: PersonalStatsUiState,
    onMemberSelect: (Int) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            personalState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            personalState.error != null -> {
                Text(
                    text = personalState.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            personalState.memberList.isEmpty() -> {
                Text(
                    text = "활동 회원이 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MemberDropdown(
                        memberList = personalState.memberList,
                        selectedMemberId = personalState.selectedMemberId,
                        onMemberSelect = onMemberSelect
                    )

                    if (personalState.selectedMemberId == null) {
                        Text(
                            text = "회원을 선택하면 통계를 확인할 수 있습니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    } else {
                        personalState.personalStats?.let { stats ->
                            BasicStatsCard(stats = stats)
                        }

                        if (personalState.scoreTrend.isNotEmpty()) {
                            ScoreTrendCard(scoreTrend = personalState.scoreTrend)
                        }

                        if (personalState.scoreDistribution.isNotEmpty()) {
                            ScoreDistributionCard(distribution = personalState.scoreDistribution)
                        }

                        RecentGamesCard(recentScores = personalState.recentScores)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberDropdown(
    memberList: List<MemberSummary>,
    selectedMemberId: Int?,
    onMemberSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedMember = memberList.find { it.id == selectedMemberId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedMember?.name ?: "회원 선택",
            onValueChange = {},
            readOnly = true,
            label = { Text("회원") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            memberList.forEach { member ->
                DropdownMenuItem(
                    text = { Text(member.name) },
                    onClick = {
                        onMemberSelect(member.id)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun BasicStatsCard(stats: PersonalStats) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "기본 통계",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "평균",
                    value = String.format("%.1f", stats.averageScore),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "최고",
                    value = stats.highScore.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "최저",
                    value = stats.lowScore.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "게임 수",
                    value = stats.totalGames.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            StatItem(
                label = "참여 대회",
                value = stats.tournamentCount.toString()
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ScoreTrendCard(scoreTrend: List<ScoreTrendItem>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "점수 추이",
                style = MaterialTheme.typography.titleMedium
            )

            ScoreTrendChart(
                scores = scoreTrend,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
    }
}

@Composable
private fun ScoreDistributionCard(distribution: List<ScoreDistributionItem>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "점수 분포",
                style = MaterialTheme.typography.titleMedium
            )

            ScoreDistributionChart(
                distribution = distribution,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
    }
}

@Composable
private fun RecentGamesCard(recentScores: List<GameScore>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "최근 게임",
                style = MaterialTheme.typography.titleMedium
            )

            if (recentScores.isEmpty()) {
                Text(
                    text = "최근 게임 기록이 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recentScores.forEach { score ->
                        RecentGameItem(score = score)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentGameItem(score: GameScore) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = score.createdAt.format(dateFormatter),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "점수: ${score.score}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (score.handicapScore > 0) {
                    Text(
                        text = "핸디캡: ${score.handicapScore}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text(
            text = score.finalScore.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
