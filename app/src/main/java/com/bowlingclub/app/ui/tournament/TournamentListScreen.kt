package com.bowlingclub.app.ui.tournament

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bowlingclub.app.data.local.entity.Tournament
import com.bowlingclub.app.ui.theme.ActiveStatus
import com.bowlingclub.app.ui.theme.BowlingClubTheme
import com.bowlingclub.app.ui.theme.PendingStatus
import com.bowlingclub.app.ui.theme.StrikeColor
import com.bowlingclub.app.viewmodel.TournamentListUiState
import com.bowlingclub.app.viewmodel.TournamentListViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentListScreen(
    modifier: Modifier = Modifier,
    viewModel: TournamentListViewModel = hiltViewModel(),
    onTournamentClick: (Int) -> Unit = {},
    onCreateClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(-45f)
                            )
                        }
                        Text(
                            text = "정기전",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = StrikeColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "새 정기전 만들기",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        TournamentListContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onFilterChange = { viewModel.filterByStatus(it) },
            onTournamentClick = onTournamentClick
        )
    }
}

@Composable
private fun TournamentListContent(
    modifier: Modifier = Modifier,
    uiState: TournamentListUiState,
    onFilterChange: (String) -> Unit,
    onTournamentClick: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        StatusFilterRow(
            currentFilter = uiState.filterStatus,
            onFilterChange = onFilterChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )

        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.fillMaxSize())
            }
            uiState.tournaments.isEmpty() -> {
                EmptyState(
                    filterStatus = uiState.filterStatus,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                TournamentList(
                    tournaments = uiState.tournaments,
                    onTournamentClick = onTournamentClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusFilterRow(
    currentFilter: String,
    onFilterChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        "ALL" to "전체",
        "SCHEDULED" to "예정",
        "COMPLETED" to "완료"
    )

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(filters) { (status, label) ->
            val isSelected = currentFilter == status

            FilterChip(
                selected = isSelected,
                onClick = { onFilterChange(status) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    )
                },
                enabled = true,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    selectedContainerColor = when (status) {
                        "SCHEDULED" -> MaterialTheme.colorScheme.primaryContainer
                        "COMPLETED" -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedLabelColor = when (status) {
                        "SCHEDULED" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "COMPLETED" -> MaterialTheme.colorScheme.onTertiaryContainer
                        else -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    selectedBorderColor = when (status) {
                        "SCHEDULED" -> MaterialTheme.colorScheme.primary
                        "COMPLETED" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    },
                    borderWidth = 1.5.dp,
                    selectedBorderWidth = 2.dp
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun TournamentList(
    tournaments: List<Tournament>,
    onTournamentClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 88.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(tournaments, key = { it.id }) { tournament ->
            TournamentCard(
                tournament = tournament,
                onClick = { onTournamentClick(tournament.id) }
            )
        }
    }
}

@Composable
private fun TournamentCard(
    tournament: Tournament,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "card_press"
    )

    val statusColor = when (tournament.status) {
        "SCHEDULED" -> MaterialTheme.colorScheme.primary
        "IN_PROGRESS" -> PendingStatus
        "COMPLETED" -> ActiveStatus
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header with diagonal accent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                statusColor.copy(alpha = 0.15f),
                                statusColor.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .drawBehind {
                        val strokeWidth = 4.dp.toPx()

                        // Diagonal lines pattern
                        for (i in -5..15) {
                            val startX = i * 60.dp.toPx()
                            drawLine(
                                color = statusColor.copy(alpha = 0.08f),
                                start = Offset(startX, 0f),
                                end = Offset(startX + size.height, size.height),
                                strokeWidth = 2.dp.toPx()
                            )
                        }

                        // Top accent line
                        drawLine(
                            color = statusColor,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = strokeWidth
                        )
                    }
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = tournament.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        StatusBadge(
                            status = tournament.status,
                            color = statusColor
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatDate(tournament.date),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.3.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (tournament.location != null) {
                    InfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "장소",
                        value = tournament.location,
                        iconTint = statusColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoChip(
                        label = "게임 수",
                        value = "${tournament.gameCount}게임",
                        modifier = Modifier.weight(1f)
                    )

                    if (tournament.isTeamGame) {
                        InfoChip(
                            label = "단체전",
                            value = "팀 경기",
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        InfoChip(
                            label = "개인전",
                            value = "개인 경기",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (tournament.description != null) {
                    Text(
                        text = tournament.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val statusText = when (status) {
        "SCHEDULED" -> "예정"
        "IN_PROGRESS" -> "진행중"
        "COMPLETED" -> "완료"
        else -> status
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .border(
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.8.sp
            ),
            color = Color.White
        )
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = iconTint
        )
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 5.dp
            )
            Text(
                text = "정기전 정보를 불러오는 중...",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyState(
    filterStatus: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .drawBehind {
                // Bowling pin pattern in background
                val pinSize = 40.dp.toPx()
                val spacing = 80.dp.toPx()
                val color = Color.Gray.copy(alpha = 0.05f)

                for (row in 0..10) {
                    for (col in 0..6) {
                        val x = col * spacing + (row % 2) * (spacing / 2)
                        val y = row * spacing

                        drawCircle(
                            color = color,
                            radius = pinSize / 2,
                            center = Offset(x, y)
                        )
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .rotate(-45f),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            }

            Text(
                text = when (filterStatus) {
                    "SCHEDULED" -> "예정된 정기전이 없습니다"
                    "COMPLETED" -> "완료된 정기전이 없습니다"
                    else -> "등록된 정기전이 없습니다"
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (filterStatus == "ALL") {
                    "오른쪽 하단 버튼을 눌러\n새 정기전을 만들어보세요"
                } else {
                    "다른 필터를 선택하거나\n새 정기전을 만들어보세요"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    return date.format(formatter)
}

@Preview(showBackground = true)
@Composable
private fun TournamentListScreenPreview() {
    val sampleTournaments = listOf(
        Tournament(
            id = 1,
            name = "2024 봄 정기전",
            date = LocalDate.of(2024, 3, 15),
            location = "스트라이크 볼링장",
            gameCount = 3,
            isTeamGame = false,
            handicapEnabled = true,
            status = "SCHEDULED",
            description = "봄맞이 개인전 정기전입니다. 핸디캡이 적용됩니다.",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        ),
        Tournament(
            id = 2,
            name = "단체전 토너먼트",
            date = LocalDate.of(2024, 2, 10),
            location = "메가 볼링센터",
            gameCount = 5,
            isTeamGame = true,
            handicapEnabled = false,
            status = "IN_PROGRESS",
            description = "3인 1조 단체전입니다.",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        ),
        Tournament(
            id = 3,
            name = "신년 챔피언십",
            date = LocalDate.of(2024, 1, 5),
            location = "프리미어 볼링",
            gameCount = 4,
            isTeamGame = false,
            handicapEnabled = true,
            status = "COMPLETED",
            description = "2024년 첫 정기전입니다.",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    )

    BowlingClubTheme {
        TournamentListContent(
            uiState = TournamentListUiState(tournaments = sampleTournaments),
            onFilterChange = {},
            onTournamentClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TournamentListEmptyStatePreview() {
    BowlingClubTheme {
        TournamentListContent(
            uiState = TournamentListUiState(
                tournaments = emptyList(),
                filterStatus = "ALL"
            ),
            onFilterChange = {},
            onTournamentClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TournamentCardPreview() {
    BowlingClubTheme {
        Surface {
            TournamentCard(
                tournament = Tournament(
                    id = 1,
                    name = "2024 봄 정기전",
                    date = LocalDate.of(2024, 3, 15),
                    location = "스트라이크 볼링장",
                    gameCount = 3,
                    isTeamGame = false,
                    handicapEnabled = true,
                    status = "SCHEDULED",
                    description = "봄맞이 개인전 정기전입니다. 핸디캡이 적용됩니다.",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                onClick = {}
            )
        }
    }
}
