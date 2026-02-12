package com.bowlingclub.app.ui.tournament

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import android.graphics.Bitmap
import androidx.compose.ui.platform.LocalContext
import com.bowlingclub.app.data.local.entity.Member
import com.bowlingclub.app.data.local.entity.Tournament
import com.bowlingclub.app.ui.components.SharePreviewDialog
import com.bowlingclub.app.util.RankingImageGenerator
import com.bowlingclub.app.util.ShareUtil
import com.bowlingclub.app.util.ScheduleShareUtil
import com.bowlingclub.app.ui.theme.ActiveStatus
import com.bowlingclub.app.ui.theme.BowlingClubTheme
import com.bowlingclub.app.ui.theme.FemaleColor
import com.bowlingclub.app.ui.theme.InactiveStatus
import com.bowlingclub.app.ui.theme.MaleColor
import com.bowlingclub.app.ui.theme.PendingStatus
import com.bowlingclub.app.viewmodel.TournamentDetailUiState
import com.bowlingclub.app.viewmodel.TournamentDetailViewModel
import com.bowlingclub.app.ui.components.RankingTable
import com.bowlingclub.app.util.RankingCalculator
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: TournamentDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToParticipants: (Int) -> Unit = {},
    onNavigateToScoreInput: (Int) -> Unit = {},
    onNavigateToTeamAssign: (Int) -> Unit = {},
    onNavigateToTeamResult: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var shareBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
        }
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.tournament?.name ?: "정기전 상세",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                actions = {
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "더보기"
                            )
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            if (uiState.scores.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("결과 공유") },
                                    onClick = {
                                        showMoreMenu = false
                                        val tournament = uiState.tournament ?: return@DropdownMenuItem
                                        val memberNames = uiState.participantMembers.mapValues { it.value.name }
                                        val rankings = RankingCalculator.calculateRanking(uiState.scores, memberNames)
                                        val bitmap = RankingImageGenerator.generateRankingBitmap(
                                            tournamentName = tournament.name,
                                            tournamentDate = formatDate(tournament.date),
                                            rankings = rankings,
                                            handicapEnabled = uiState.handicapEnabled,
                                            gameCount = tournament.gameCount
                                        )
                                        shareBitmap = bitmap
                                        showShareDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                            if (uiState.tournament?.status == "SCHEDULED") {
                                DropdownMenuItem(
                                    text = { Text("일정 공유") },
                                    onClick = {
                                        showMoreMenu = false
                                        val tournament = uiState.tournament ?: return@DropdownMenuItem
                                        ScheduleShareUtil.shareSchedule(
                                            context = context,
                                            tournamentName = tournament.name,
                                            date = tournament.date,
                                            location = tournament.location,
                                            gameCount = tournament.gameCount,
                                            isTeamGame = tournament.isTeamGame,
                                            participantNames = uiState.participantMembers.values.map { it.name }
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("삭제") },
                                onClick = {
                                    showMoreMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
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
        TournamentDetailContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onStatusChange = viewModel::updateStatus,
            onNavigateToParticipants = onNavigateToParticipants,
            onNavigateToScoreInput = onNavigateToScoreInput,
            onNavigateToTeamAssign = onNavigateToTeamAssign,
            onNavigateToTeamResult = onNavigateToTeamResult
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            tournamentName = uiState.tournament?.name ?: "",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteTournament()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showShareDialog) {
        SharePreviewDialog(
            bitmap = shareBitmap,
            onShareImage = {
                showShareDialog = false
                shareBitmap?.recycle()
                shareBitmap = null
                val tournament = uiState.tournament ?: return@SharePreviewDialog
                val memberNames = uiState.participantMembers.mapValues { it.value.name }
                val rankings = RankingCalculator.calculateRanking(uiState.scores, memberNames)
                val imageUri = RankingImageGenerator.generateRankingImage(
                    context = context,
                    tournamentName = tournament.name,
                    tournamentDate = formatDate(tournament.date),
                    rankings = rankings,
                    handicapEnabled = uiState.handicapEnabled,
                    gameCount = tournament.gameCount
                )
                imageUri?.let { ShareUtil.shareImage(context, it, "${tournament.name} 순위표") }
            },
            onShareText = {
                showShareDialog = false
                shareBitmap?.recycle()
                shareBitmap = null
                val tournament = uiState.tournament ?: return@SharePreviewDialog
                val memberNames = uiState.participantMembers.mapValues { it.value.name }
                val rankings = RankingCalculator.calculateRanking(uiState.scores, memberNames)
                val text = ShareUtil.formatRankingText(
                    tournamentName = tournament.name,
                    tournamentDate = formatDate(tournament.date),
                    rankings = rankings,
                    handicapEnabled = uiState.handicapEnabled
                )
                ShareUtil.shareText(context, text, "${tournament.name} 순위표")
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
private fun TournamentDetailContent(
    modifier: Modifier = Modifier,
    uiState: TournamentDetailUiState,
    onStatusChange: (String) -> Unit,
    onNavigateToParticipants: (Int) -> Unit,
    onNavigateToScoreInput: (Int) -> Unit,
    onNavigateToTeamAssign: (Int) -> Unit,
    onNavigateToTeamResult: (Int) -> Unit
) {
    when {
        uiState.isLoading && uiState.tournament == null -> {
            LoadingState(modifier = Modifier.fillMaxSize())
        }
        uiState.tournament == null -> {
            ErrorState(
                message = uiState.error ?: "정기전 정보를 찾을 수 없습니다",
                modifier = Modifier.fillMaxSize()
            )
        }
        else -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    TournamentInfoCard(tournament = uiState.tournament)
                }

                item {
                    StatusTransitionCard(
                        currentStatus = uiState.tournament.status,
                        isLoading = uiState.isLoading,
                        onStatusChange = onStatusChange
                    )
                }

                item {
                    ParticipantsSection(
                        tournamentId = uiState.tournament.id,
                        participants = uiState.participantMembers.values.toList(),
                        onEditClick = { onNavigateToParticipants(uiState.tournament.id) }
                    )
                }

                item {
                    ScoreInputButton(
                        tournamentId = uiState.tournament.id,
                        status = uiState.tournament.status,
                        enabled = uiState.tournament.status == "IN_PROGRESS",
                        onClick = { onNavigateToScoreInput(uiState.tournament.id) }
                    )
                }

                // 팀전인 경우에만 팀 관련 버튼 표시
                if (uiState.tournament.isTeamGame) {
                    item {
                        TeamActionsCard(
                            tournamentId = uiState.tournament.id,
                            status = uiState.tournament.status,
                            onTeamAssign = { onNavigateToTeamAssign(uiState.tournament.id) },
                            onTeamResult = { onNavigateToTeamResult(uiState.tournament.id) }
                        )
                    }
                }

                if (uiState.scores.isNotEmpty()) {
                    item {
                        RankingSection(
                            scores = uiState.scores,
                            members = uiState.participantMembers,
                            handicapEnabled = uiState.handicapEnabled,
                            gameCount = uiState.tournament.gameCount
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TournamentInfoCard(
    tournament: Tournament,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tournament.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )

                StatusBadge(status = tournament.status)
            }

            InfoRow(
                icon = Icons.Default.EventAvailable,
                label = "날짜",
                value = formatDate(tournament.date)
            )

            if (!tournament.location.isNullOrBlank()) {
                InfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "장소",
                    value = tournament.location
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoChip(
                    text = "게임 ${tournament.gameCount}개",
                    modifier = Modifier.weight(1f)
                )
                if (tournament.isTeamGame) {
                    InfoChip(
                        text = "팀전",
                        modifier = Modifier.weight(1f)
                    )
                }
                if (tournament.handicapEnabled) {
                    InfoChip(
                        text = "핸디캡",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (!tournament.description.isNullOrBlank()) {
                Text(
                    text = tournament.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun InfoChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusText) = when (status) {
        "SCHEDULED" -> PendingStatus to "예정"
        "IN_PROGRESS" -> ActiveStatus to "진행 중"
        "COMPLETED" -> MaterialTheme.colorScheme.tertiary to "완료"
        else -> InactiveStatus to "알 수 없음"
    }

    Badge(
        modifier = modifier,
        containerColor = statusColor,
        contentColor = Color.White
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun StatusTransitionCard(
    currentStatus: String,
    isLoading: Boolean,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "상태 변경",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            when (currentStatus) {
                "SCHEDULED" -> {
                    Button(
                        onClick = { onStatusChange("IN_PROGRESS") },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "정기전 시작",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                "IN_PROGRESS" -> {
                    Button(
                        onClick = { onStatusChange("COMPLETED") },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "정기전 종료",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                "COMPLETED" -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "정기전이 완료되었습니다",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantsSection(
    tournamentId: Int,
    participants: List<Member>,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "참가자",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = "${participants.size}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                OutlinedButton(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "편집",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (participants.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "아직 참가자가 없습니다\n참가자 편집 버튼을 눌러 추가하세요",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    participants.forEach { member ->
                        ParticipantItem(member = member)
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantItem(
    member: Member,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MemberAvatar(
                name = member.name,
                gender = member.gender,
                modifier = Modifier.size(40.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (member.nickname != null) {
                    Text(
                        text = "\"${member.nickname}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberAvatar(
    name: String,
    gender: String,
    modifier: Modifier = Modifier
) {
    val gradientColors = if (gender == "M") {
        listOf(MaleColor.copy(alpha = 0.8f), MaleColor)
    } else {
        listOf(FemaleColor.copy(alpha = 0.8f), FemaleColor)
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(colors = gradientColors)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.toString()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

@Composable
private fun RankingSection(
    scores: List<com.bowlingclub.app.data.local.entity.GameScore>,
    members: Map<Int, Member>,
    handicapEnabled: Boolean,
    gameCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "순위표",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val memberNames = members.mapValues { it.value.name }
            val rankings = RankingCalculator.calculateRanking(scores, memberNames)

            RankingTable(
                rankings = rankings,
                handicapEnabled = handicapEnabled,
                gameCount = gameCount
            )
        }
    }
}

@Composable
private fun TeamActionsCard(
    tournamentId: Int,
    status: String,
    onTeamAssign: () -> Unit,
    onTeamResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "팀 관리",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = onTeamAssign,
                    enabled = status != "COMPLETED",
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "팀 배정",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                OutlinedButton(
                    onClick = onTeamResult,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "팀 결과",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            if (status == "COMPLETED") {
                Text(
                    text = "완료된 정기전은 팀 배정을 변경할 수 없습니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ScoreInputButton(
    tournamentId: Int,
    status: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "점수 입력",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (enabled) "점수 입력하기" else "정기전 진행 중일 때 입력 가능",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            if (!enabled) {
                Text(
                    text = when (status) {
                        "SCHEDULED" -> "정기전을 시작한 후 점수를 입력할 수 있습니다"
                        "COMPLETED" -> "완료된 정기전은 점수를 입력할 수 없습니다"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    tournamentName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "정기전 삭제",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "\"$tournamentName\" 정기전을 삭제하시겠습니까?",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "관련된 모든 점수 기록도 함께 삭제됩니다. 이 작업은 되돌릴 수 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("삭제")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
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
                text = "정기전 정보를 불러오는 중...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
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
                text = "❌",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp)
            )
            Text(
                text = "오류 발생",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
    return date.format(formatter)
}

@Preview(showBackground = true)
@Composable
private fun TournamentDetailScreenPreview() {
    val sampleTournament = Tournament(
        id = 1,
        name = "2024년 1월 정기전",
        date = LocalDate.of(2024, 1, 20),
        location = "강남볼링장",
        gameCount = 4,
        isTeamGame = false,
        handicapEnabled = true,
        status = "IN_PROGRESS",
        description = "신년 첫 정기전입니다",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    val sampleMembers = listOf(
        Member(
            id = 1,
            name = "김철수",
            nickname = "볼링왕",
            gender = "M",
            phoneNumber = "010-1234-5678",
            isActive = true,
            joinDate = LocalDate.of(2024, 1, 15),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        ),
        Member(
            id = 2,
            name = "이영희",
            nickname = "스트라이크퀸",
            gender = "F",
            phoneNumber = "010-9876-5432",
            isActive = true,
            joinDate = LocalDate.of(2024, 2, 20),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    )

    BowlingClubTheme {
        TournamentDetailContent(
            uiState = TournamentDetailUiState(
                tournament = sampleTournament,
                participantMembers = sampleMembers.associateBy { it.id }
            ),
            onStatusChange = {},
            onNavigateToParticipants = {},
            onNavigateToScoreInput = {},
            onNavigateToTeamAssign = {},
            onNavigateToTeamResult = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TournamentDetailScheduledPreview() {
    BowlingClubTheme {
        TournamentDetailContent(
            uiState = TournamentDetailUiState(
                tournament = Tournament(
                    id = 1,
                    name = "2024년 2월 정기전",
                    date = LocalDate.of(2024, 2, 15),
                    location = "서울볼링센터",
                    gameCount = 3,
                    isTeamGame = true,
                    handicapEnabled = false,
                    status = "SCHEDULED",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
            ),
            onStatusChange = {},
            onNavigateToParticipants = {},
            onNavigateToScoreInput = {},
            onNavigateToTeamAssign = {},
            onNavigateToTeamResult = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TournamentDetailCompletedPreview() {
    BowlingClubTheme {
        TournamentDetailContent(
            uiState = TournamentDetailUiState(
                tournament = Tournament(
                    id = 1,
                    name = "2023년 12월 정기전",
                    date = LocalDate.of(2023, 12, 30),
                    location = "부산볼링장",
                    gameCount = 4,
                    isTeamGame = false,
                    handicapEnabled = true,
                    status = "COMPLETED",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
            ),
            onStatusChange = {},
            onNavigateToParticipants = {},
            onNavigateToScoreInput = {},
            onNavigateToTeamAssign = {},
            onNavigateToTeamResult = {}
        )
    }
}
