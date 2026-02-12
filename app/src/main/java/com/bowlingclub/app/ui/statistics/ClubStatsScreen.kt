package com.bowlingclub.app.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bowlingclub.app.data.model.ClubStats
import com.bowlingclub.app.data.model.MemberRankingItem
import com.bowlingclub.app.ui.components.MemberComparisonChart
import com.bowlingclub.app.ui.theme.BronzeRank
import com.bowlingclub.app.ui.theme.GoldRank
import com.bowlingclub.app.ui.theme.SilverRank
import com.bowlingclub.app.viewmodel.ClubStatsUiState

@Composable
fun ClubStatsContent(
    clubState: ClubStatsUiState
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            clubState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            clubState.error != null -> {
                Text(
                    text = clubState.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            else -> {
                ClubStatsScrollableContent(
                    clubStats = clubState.clubStats,
                    memberRankings = clubState.memberRankings
                )
            }
        }
    }
}

@Composable
private fun ClubStatsScrollableContent(
    clubStats: ClubStats?,
    memberRankings: List<MemberRankingItem>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        clubStats?.let {
            ClubStatisticsCard(clubStats = it)
        }

        MemberComparisonChartCard(memberRankings = memberRankings)

        MemberRankingListCard(memberRankings = memberRankings)
    }
}

@Composable
private fun ClubStatisticsCard(clubStats: ClubStats) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "클럽 통계",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatItem(
                        label = "전체 게임 수",
                        value = "${clubStats.totalGames}",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        label = "전체 평균",
                        value = String.format("%.1f", clubStats.overallAverage),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatItem(
                        label = "최고 점수",
                        value = "${clubStats.highestScore}",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        label = "활동 회원",
                        value = "${clubStats.activeMemberCount} 명",
                        modifier = Modifier.weight(1f)
                    )
                }

                StatItem(
                    label = "총 대회",
                    value = "${clubStats.totalTournaments} 회",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MemberComparisonChartCard(memberRankings: List<MemberRankingItem>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "회원 평균 비교",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                MemberComparisonChart(rankings = memberRankings)
            }
        }
    }
}

@Composable
private fun MemberRankingListCard(memberRankings: List<MemberRankingItem>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "회원 랭킹",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (memberRankings.isEmpty()) {
                Text(
                    text = "등록된 점수가 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                RankingHeader()

                memberRankings.forEachIndexed { index, ranking ->
                    HorizontalDivider()
                    RankingItem(
                        rank = index + 1,
                        rankingItem = ranking
                    )
                }
            }
        }
    }
}

@Composable
private fun RankingHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "순위",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )
        Text(
            text = "이름",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "평균",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = "최고",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(50.dp)
        )
        Text(
            text = "게임수",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
private fun RankingItem(
    rank: Int,
    rankingItem: MemberRankingItem
) {
    val backgroundColor = when (rank) {
        1 -> GoldRank.copy(alpha = 0.2f)
        2 -> SilverRank.copy(alpha = 0.2f)
        3 -> BronzeRank.copy(alpha = 0.2f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (rank <= 3) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.width(40.dp)
        )
        Text(
            text = rankingItem.memberName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (rank <= 3) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = String.format("%.1f", rankingItem.averageScore),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = "${rankingItem.highScore}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(50.dp)
        )
        Text(
            text = "${rankingItem.gamesPlayed}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(60.dp)
        )
    }
}
