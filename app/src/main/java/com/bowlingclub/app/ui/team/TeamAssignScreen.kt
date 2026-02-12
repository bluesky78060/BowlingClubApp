package com.bowlingclub.app.ui.team

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bowlingclub.app.ui.theme.BowlingClubTheme
import com.bowlingclub.app.util.TeamAssignment
import com.bowlingclub.app.viewmodel.AssignMethod
import com.bowlingclub.app.viewmodel.TeamAssignUiState
import com.bowlingclub.app.viewmodel.TeamAssignViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamAssignScreen(
    modifier: Modifier = Modifier,
    viewModel: TeamAssignViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
            viewModel.clearError()
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
                        text = "팀 편성",
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
        TeamAssignContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onUpdateTeamCount = viewModel::updateTeamCount,
            onUpdateAssignMethod = viewModel::updateAssignMethod,
            onAutoAssign = viewModel::autoAssign,
            onMovePlayer = viewModel::movePlayer,
            onRemovePlayer = viewModel::removePlayerFromTeam,
            onSave = viewModel::saveTeams,
            getUnassignedMembers = viewModel::getUnassignedMembers
        )
    }
}

@Composable
private fun TeamAssignContent(
    modifier: Modifier = Modifier,
    uiState: TeamAssignUiState,
    onUpdateTeamCount: (Int) -> Unit,
    onUpdateAssignMethod: (AssignMethod) -> Unit,
    onAutoAssign: () -> Unit,
    onMovePlayer: (Int, Int) -> Unit,
    onRemovePlayer: (Int) -> Unit,
    onSave: () -> Unit,
    getUnassignedMembers: () -> List<Int>
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.isLoading && uiState.availableMembers.isEmpty()) {
            LoadingState(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ControlsSection(
                        teamCount = uiState.teamCount,
                        assignMethod = uiState.assignMethod,
                        onUpdateTeamCount = onUpdateTeamCount,
                        onUpdateAssignMethod = onUpdateAssignMethod,
                        onAutoAssign = onAutoAssign
                    )
                }

                items(uiState.teams.size) { index ->
                    val team = uiState.teams[index]
                    TeamCard(
                        teamIndex = index,
                        team = team,
                        memberNames = uiState.availableMembers,
                        memberAverages = uiState.memberAverages,
                        onRemovePlayer = onRemovePlayer
                    )
                }

                item {
                    val unassignedMembers = getUnassignedMembers()
                    if (unassignedMembers.isNotEmpty()) {
                        UnassignedSection(
                            unassignedMemberIds = unassignedMembers,
                            memberNames = uiState.availableMembers,
                            memberAverages = uiState.memberAverages,
                            teams = uiState.teams,
                            onMovePlayer = onMovePlayer
                        )
                    }
                }
            }

            SaveButton(
                enabled = !uiState.isLoading && uiState.teams.isNotEmpty(),
                teamCount = uiState.teams.size,
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun ControlsSection(
    teamCount: Int,
    assignMethod: AssignMethod,
    onUpdateTeamCount: (Int) -> Unit,
    onUpdateAssignMethod: (AssignMethod) -> Unit,
    onAutoAssign: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Team count selector
            Text(
                text = "팀 수",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onUpdateTeamCount(teamCount - 1) },
                    enabled = teamCount > 2,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (teamCount > 2) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "팀 수 감소",
                        tint = if (teamCount > 2) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$teamCount 팀",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        ),
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = { onUpdateTeamCount(teamCount + 1) },
                    enabled = teamCount < 8,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (teamCount < 8) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "팀 수 증가",
                        tint = if (teamCount < 8) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Assignment method chips
            Text(
                text = "편성 방식",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssignMethod.entries.forEach { method ->
                    val isSelected = method == assignMethod
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateAssignMethod(method) },
                        label = {
                            Text(
                                text = when (method) {
                                    AssignMethod.SNAKE_DRAFT -> "스네이크 드래프트"
                                    AssignMethod.RANDOM -> "랜덤"
                                    AssignMethod.MANUAL -> "수동"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        enabled = true,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Auto assign button
            if (assignMethod != AssignMethod.MANUAL) {
                Button(
                    onClick = onAutoAssign,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "자동 편성",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamCard(
    teamIndex: Int,
    team: TeamAssignment,
    memberNames: Map<Int, String>,
    memberAverages: Map<Int, Double>,
    onRemovePlayer: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val teamAverage = if (team.memberIds.isNotEmpty()) {
        team.memberIds.mapNotNull { memberAverages[it] }.average()
    } else {
        0.0
    }

    val gradientColors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Team header with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = gradientColors
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = team.teamName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${team.memberIds.size}명",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "평균 점수",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "%.1f".format(teamAverage),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Team members list
            if (team.memberIds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "팀원 없음",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    team.memberIds.forEach { memberId ->
                        MemberChip(
                            memberName = memberNames[memberId] ?: "Unknown",
                            memberAverage = memberAverages[memberId] ?: 0.0,
                            onRemove = { onRemovePlayer(memberId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberChip(
    memberName: String,
    memberAverage: Double,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = memberName.firstOrNull()?.toString()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = memberName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "평균: %.1f".format(memberAverage),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "제거",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun UnassignedSection(
    unassignedMemberIds: List<Int>,
    memberNames: Map<Int, String>,
    memberAverages: Map<Int, Double>,
    teams: List<TeamAssignment>,
    onMovePlayer: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedMemberId by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "미배정 멤버 (${unassignedMemberIds.size}명)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            unassignedMemberIds.forEach { memberId ->
                UnassignedMemberCard(
                    memberId = memberId,
                    memberName = memberNames[memberId] ?: "Unknown",
                    memberAverage = memberAverages[memberId] ?: 0.0,
                    teams = teams,
                    isExpanded = expandedMemberId == memberId,
                    onToggleExpand = {
                        expandedMemberId = if (expandedMemberId == memberId) null else memberId
                    },
                    onMoveToTeam = { teamIndex ->
                        onMovePlayer(memberId, teamIndex)
                        expandedMemberId = null
                    }
                )
            }
        }
    }
}

@Composable
private fun UnassignedMemberCard(
    memberId: Int,
    memberName: String,
    memberAverage: Double,
    teams: List<TeamAssignment>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onMoveToTeam: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = memberName.firstOrNull()?.toString()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = memberName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "평균: %.1f".format(memberAverage),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = if (isExpanded) "▲" else "▼",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "팀 선택",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    teams.forEachIndexed { index, team ->
                        Button(
                            onClick = { onMoveToTeam(index) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${team.teamName} (${team.memberIds.size}명)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveButton(
    enabled: Boolean,
    teamCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (enabled) {
            Text(
                text = "저장 ($teamCount 팀)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
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
                text = "참가자 정보를 불러오는 중...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamAssignScreenPreview() {
    val sampleState = TeamAssignUiState(
        tournamentId = 1,
        availableMembers = mapOf(
            1 to "김철수",
            2 to "이영희",
            3 to "박민수",
            4 to "정수진",
            5 to "최동욱",
            6 to "강미라"
        ),
        memberAverages = mapOf(
            1 to 185.0,
            2 to 165.0,
            3 to 178.0,
            4 to 192.0,
            5 to 155.0,
            6 to 170.0
        ),
        teamCount = 3,
        teams = listOf(
            TeamAssignment("팀 1", listOf(1, 4)),
            TeamAssignment("팀 2", listOf(2, 3)),
            TeamAssignment("팀 3", listOf(5))
        ),
        assignMethod = AssignMethod.SNAKE_DRAFT
    )

    BowlingClubTheme {
        TeamAssignContent(
            uiState = sampleState,
            onUpdateTeamCount = {},
            onUpdateAssignMethod = {},
            onAutoAssign = {},
            onMovePlayer = { _, _ -> },
            onRemovePlayer = {},
            onSave = {},
            getUnassignedMembers = { listOf(6) }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamAssignEmptyPreview() {
    val emptyState = TeamAssignUiState(
        tournamentId = 1,
        availableMembers = mapOf(
            1 to "김철수",
            2 to "이영희",
            3 to "박민수"
        ),
        memberAverages = mapOf(
            1 to 185.0,
            2 to 165.0,
            3 to 178.0
        ),
        teamCount = 2,
        teams = emptyList(),
        assignMethod = AssignMethod.SNAKE_DRAFT
    )

    BowlingClubTheme {
        TeamAssignContent(
            uiState = emptyState,
            onUpdateTeamCount = {},
            onUpdateAssignMethod = {},
            onAutoAssign = {},
            onMovePlayer = { _, _ -> },
            onRemovePlayer = {},
            onSave = {},
            getUnassignedMembers = { listOf(1, 2, 3) }
        )
    }
}
