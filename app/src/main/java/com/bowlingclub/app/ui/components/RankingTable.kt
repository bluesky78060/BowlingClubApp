package com.bowlingclub.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bowlingclub.app.data.model.RankingResult
import com.bowlingclub.app.ui.theme.BowlingClubTheme
import com.bowlingclub.app.ui.theme.BronzeRank
import com.bowlingclub.app.ui.theme.GoldRank
import com.bowlingclub.app.ui.theme.SilverRank

@Composable
fun RankingTable(
    modifier: Modifier = Modifier,
    rankings: List<RankingResult>,
    handicapEnabled: Boolean = false,
    gameCount: Int = 3
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            TableHeader(handicapEnabled = handicapEnabled, gameCount = gameCount)

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outline
            )

            rankings.forEach { ranking ->
                TableRow(
                    ranking = ranking,
                    handicapEnabled = handicapEnabled
                )

                if (ranking != rankings.last()) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TableHeader(
    handicapEnabled: Boolean,
    gameCount: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableHeaderCell(
            text = "순위",
            modifier = Modifier.width(50.dp),
            weight = null
        )

        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        TableHeaderCell(
            text = "이름",
            modifier = Modifier.width(80.dp),
            weight = null
        )

        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        (1..gameCount).forEach { gameNum ->
            TableHeaderCell(
                text = "${gameNum}G",
                weight = 1f
            )
            if (gameNum < gameCount) {
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            }
        }

        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        TableHeaderCell(
            text = "합계",
            weight = 1f
        )

        if (handicapEnabled) {
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            TableHeaderCell(
                text = "핸디캡",
                weight = 1f
            )

            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            TableHeaderCell(
                text = "순점수",
                weight = 1f
            )
        }
    }
}

@Composable
private fun RowScope.TableHeaderCell(
    text: String,
    modifier: Modifier = Modifier,
    weight: Float? = null
) {
    val cellModifier = if (weight != null) {
        modifier.weight(weight)
    } else {
        modifier
    }

    Box(
        modifier = cellModifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TableRow(
    ranking: RankingResult,
    handicapEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (ranking.rank) {
        1 -> GoldRank.copy(alpha = 0.15f)
        2 -> SilverRank.copy(alpha = 0.2f)
        3 -> BronzeRank.copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    val rankColor = when (ranking.rank) {
        1 -> GoldRank
        2 -> SilverRank
        3 -> BronzeRank
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableDataCell(
            text = ranking.rank.toString(),
            modifier = Modifier.width(50.dp),
            weight = null,
            isBold = ranking.rank <= 3,
            textColor = rankColor
        )

        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )

        TableDataCell(
            text = ranking.memberName,
            modifier = Modifier.width(80.dp),
            weight = null,
            isBold = ranking.rank <= 3
        )

        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )

        ranking.gameScores.forEachIndexed { index, score ->
            val isHighGame = score == ranking.highGame && score > 0

            TableDataCell(
                text = score.toString(),
                weight = 1f,
                isBold = isHighGame,
                textColor = if (isHighGame) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            if (index < ranking.gameScores.size - 1) {
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
        }

        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )

        TableDataCell(
            text = ranking.totalScore.toString(),
            weight = 1f,
            isBold = true,
            textColor = MaterialTheme.colorScheme.primary
        )

        if (handicapEnabled) {
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            TableDataCell(
                text = ranking.handicapTotal.toString(),
                weight = 1f,
                textColor = MaterialTheme.colorScheme.tertiary
            )

            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            TableDataCell(
                text = ranking.finalTotal.toString(),
                weight = 1f,
                isBold = true,
                textColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RowScope.TableDataCell(
    text: String,
    modifier: Modifier = Modifier,
    weight: Float? = null,
    isBold: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val cellModifier = if (weight != null) {
        modifier.weight(weight)
    } else {
        modifier
    }

    Box(
        modifier = cellModifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            ),
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RankingTablePreview() {
    BowlingClubTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            RankingTable(
                rankings = listOf(
                    RankingResult(
                        rank = 1,
                        memberId = 1,
                        memberName = "김철수",
                        gameScores = listOf(220, 195, 210),
                        totalScore = 625,
                        handicapTotal = 0,
                        finalTotal = 625,
                        highGame = 220,
                        average = 208.3
                    ),
                    RankingResult(
                        rank = 2,
                        memberId = 2,
                        memberName = "이영희",
                        gameScores = listOf(180, 200, 195),
                        totalScore = 575,
                        handicapTotal = 0,
                        finalTotal = 575,
                        highGame = 200,
                        average = 191.7
                    ),
                    RankingResult(
                        rank = 3,
                        memberId = 3,
                        memberName = "박민수",
                        gameScores = listOf(175, 165, 180),
                        totalScore = 520,
                        handicapTotal = 0,
                        finalTotal = 520,
                        highGame = 180,
                        average = 173.3
                    ),
                    RankingResult(
                        rank = 4,
                        memberId = 4,
                        memberName = "정수진",
                        gameScores = listOf(150, 160, 155),
                        totalScore = 465,
                        handicapTotal = 0,
                        finalTotal = 465,
                        highGame = 160,
                        average = 155.0
                    )
                ),
                handicapEnabled = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RankingTableWithHandicapPreview() {
    BowlingClubTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            RankingTable(
                rankings = listOf(
                    RankingResult(
                        rank = 1,
                        memberId = 1,
                        memberName = "김철수",
                        gameScores = listOf(220, 195, 210),
                        totalScore = 625,
                        handicapTotal = 30,
                        finalTotal = 655,
                        highGame = 220,
                        average = 208.3
                    ),
                    RankingResult(
                        rank = 2,
                        memberId = 2,
                        memberName = "이영희",
                        gameScores = listOf(180, 200, 195),
                        totalScore = 575,
                        handicapTotal = 45,
                        finalTotal = 620,
                        highGame = 200,
                        average = 191.7
                    ),
                    RankingResult(
                        rank = 3,
                        memberId = 3,
                        memberName = "박민수",
                        gameScores = listOf(175, 165, 180),
                        totalScore = 520,
                        handicapTotal = 60,
                        finalTotal = 580,
                        highGame = 180,
                        average = 173.3
                    )
                ),
                handicapEnabled = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RankingTableSingleEntryPreview() {
    BowlingClubTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            RankingTable(
                rankings = listOf(
                    RankingResult(
                        rank = 1,
                        memberId = 1,
                        memberName = "김철수",
                        gameScores = listOf(220, 195, 210),
                        totalScore = 625,
                        handicapTotal = 0,
                        finalTotal = 625,
                        highGame = 220,
                        average = 208.3
                    )
                ),
                handicapEnabled = false
            )
        }
    }
}
