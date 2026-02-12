package com.bowlingclub.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.bowlingclub.app.data.model.MemberRankingItem
import com.bowlingclub.app.data.model.ScoreDistributionItem
import com.bowlingclub.app.data.model.ScoreTrendItem
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 * ScoreTrendChart - 점수 추이 라인 차트
 *
 * @param modifier Modifier
 * @param scores 점수 추이 데이터 리스트
 * @param lineColor 라인 색상
 * @param fillColor 채우기 색상 (그라데이션)
 */
@Composable
fun ScoreTrendChart(
    modifier: Modifier = Modifier,
    scores: List<ScoreTrendItem>,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
) {
    if (scores.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "데이터가 없습니다",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(false)
                setDrawGridBackground(false)

                // X축 설정
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    labelRotationAngle = 0f
                }

                // 왼쪽 Y축 설정
                axisLeft.apply {
                    axisMinimum = 0f
                    axisMaximum = 300f
                    setDrawGridLines(true)
                    gridLineWidth = 0.5f
                }

                // 오른쪽 Y축 비활성화
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = scores.mapIndexed { index, item ->
                Entry(index.toFloat(), item.score.toFloat())
            }

            val lineArgbColor = lineColor.toArgb()
            val fillArgbColor = fillColor.toArgb()

            val dataSet = LineDataSet(entries, "점수 추이").apply {
                color = lineArgbColor
                lineWidth = 2.5f
                setCircleColor(lineArgbColor)
                circleRadius = 4f
                setDrawCircleHole(false)
                setDrawValues(true)
                valueTextSize = 10f

                // 부드러운 곡선
                mode = LineDataSet.Mode.CUBIC_BEZIER

                // 그라데이션 fill
                setDrawFilled(true)
                setFillColor(fillArgbColor)
                fillAlpha = 25
            }

            val lineData = LineData(dataSet)

            chart.data = lineData
            chart.animateY(500)
            chart.invalidate()
        },
        modifier = modifier
    )
}

/**
 * ScoreDistributionChart - 점수 분포 막대 차트
 *
 * @param modifier Modifier
 * @param distribution 점수 분포 데이터 리스트
 * @param barColor 막대 색상
 */
@Composable
fun ScoreDistributionChart(
    modifier: Modifier = Modifier,
    distribution: List<ScoreDistributionItem>,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    if (distribution.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "데이터가 없습니다",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(false)
                setDrawGridBackground(false)
                setFitBars(true)

                // X축 설정
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    labelRotationAngle = 45f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt()
                            return if (index >= 0 && index < distribution.size) {
                                distribution[index].range
                            } else ""
                        }
                    }
                }

                // 왼쪽 Y축 설정
                axisLeft.apply {
                    axisMinimum = 0f
                    setDrawGridLines(true)
                    gridLineWidth = 0.5f
                    granularity = 1f
                }

                // 오른쪽 Y축 비활성화
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = distribution.mapIndexed { index, item ->
                BarEntry(index.toFloat(), item.count.toFloat())
            }

            val dataSet = BarDataSet(entries, "점수 분포").apply {
                color = barColor.toArgb()
                valueTextSize = 10f
                setDrawValues(true)
            }

            val barData = BarData(dataSet)
            barData.barWidth = 0.8f

            chart.data = barData
            chart.animateY(500)
            chart.invalidate()
        },
        modifier = modifier
    )
}

/**
 * MemberComparisonChart - 회원 비교 수평 막대 차트
 *
 * @param modifier Modifier
 * @param rankings 회원 랭킹 데이터 리스트 (상위 10명만 표시)
 * @param barColor 막대 색상
 */
@Composable
fun MemberComparisonChart(
    modifier: Modifier = Modifier,
    rankings: List<MemberRankingItem>,
    barColor: Color = MaterialTheme.colorScheme.secondary
) {
    if (rankings.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "데이터가 없습니다",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // 상위 10명만 표시
    val topRankings = rankings.take(10)

    AndroidView(
        factory = { context ->
            HorizontalBarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(false)
                setDrawGridBackground(false)
                setFitBars(true)

                // X축 설정 (수평이므로 점수)
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(true)
                    gridLineWidth = 0.5f
                    axisMinimum = 0f
                }

                // Y축 설정 (수평이므로 회원 이름)
                axisLeft.apply {
                    setDrawGridLines(false)
                    granularity = 1f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt()
                            return if (index >= 0 && index < topRankings.size) {
                                topRankings[index].memberName
                            } else ""
                        }
                    }
                }

                // 오른쪽 Y축 비활성화
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = topRankings.mapIndexed { index, item ->
                BarEntry(index.toFloat(), item.averageScore.toFloat())
            }

            val dataSet = BarDataSet(entries, "회원 비교").apply {
                color = barColor.toArgb()
                valueTextSize = 10f
                setDrawValues(true)
            }

            val barData = BarData(dataSet)
            barData.barWidth = 0.8f

            chart.data = barData
            chart.animateXY(500, 500)
            chart.invalidate()
        },
        modifier = modifier
    )
}
