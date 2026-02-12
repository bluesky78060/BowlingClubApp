package com.bowlingclub.app.ui.tournament

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bowlingclub.app.data.local.entity.Member
import com.bowlingclub.app.ui.theme.ActiveStatus
import com.bowlingclub.app.ui.theme.BowlingClubTheme
import com.bowlingclub.app.ui.theme.FemaleColor
import com.bowlingclub.app.ui.theme.InactiveStatus
import com.bowlingclub.app.ui.theme.MaleColor
import com.bowlingclub.app.viewmodel.ParticipantCheckUiState
import com.bowlingclub.app.viewmodel.ParticipantCheckViewModel
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantCheckScreen(
    modifier: Modifier = Modifier,
    viewModel: ParticipantCheckViewModel = hiltViewModel(),
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
            viewModel.resetSaved()
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "참가자 선택",
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
        ParticipantCheckContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onToggleMember = viewModel::toggleMember,
            onSelectAll = viewModel::selectAll,
            onDeselectAll = viewModel::deselectAll,
            handicapMap = uiState.handicapMap,
            tournamentHandicapEnabled = uiState.tournamentHandicapEnabled,
            onHandicapChange = viewModel::updateHandicap,
            onSave = viewModel::saveParticipants
        )
    }
}

@Composable
private fun ParticipantCheckContent(
    modifier: Modifier = Modifier,
    uiState: ParticipantCheckUiState,
    onToggleMember: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    handicapMap: Map<Int, Int>,
    tournamentHandicapEnabled: Boolean,
    onHandicapChange: (Int, Int) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.isLoading && uiState.allMembers.isEmpty()) {
            LoadingState(modifier = Modifier.fillMaxSize())
        } else {
            SelectionControls(
                selectedCount = uiState.selectedMemberIds.size,
                totalCount = uiState.allMembers.size,
                onSelectAll = onSelectAll,
                onDeselectAll = onDeselectAll,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            MemberSelectionList(
                members = uiState.allMembers,
                selectedMemberIds = uiState.selectedMemberIds,
                onToggleMember = onToggleMember,
                handicapMap = handicapMap,
                tournamentHandicapEnabled = tournamentHandicapEnabled,
                onHandicapChange = onHandicapChange,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            SaveButton(
                enabled = !uiState.isLoading,
                selectedCount = uiState.selectedMemberIds.size,
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun SelectionControls(
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "선택된 인원",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "${selectedCount}명 / ${totalCount}명",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onSelectAll,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "모두 선택",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                OutlinedButton(
                    onClick = onDeselectAll,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "모두 해제",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberSelectionList(
    members: List<Member>,
    selectedMemberIds: Set<Int>,
    onToggleMember: (Int) -> Unit,
    handicapMap: Map<Int, Int>,
    tournamentHandicapEnabled: Boolean,
    onHandicapChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (members.isEmpty()) {
        EmptyState(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(members, key = { it.id }) { member ->
                MemberSelectionCard(
                    member = member,
                    isSelected = selectedMemberIds.contains(member.id),
                    onToggle = { onToggleMember(member.id) },
                    handicap = handicapMap[member.id] ?: 0,
                    onHandicapChange = { handicap -> onHandicapChange(member.id, handicap) },
                    showHandicap = tournamentHandicapEnabled
                )
            }
        }
    }
}

@Composable
private fun MemberSelectionCard(
    member: Member,
    isSelected: Boolean,
    onToggle: () -> Unit,
    handicap: Int,
    onHandicapChange: (Int) -> Unit,
    showHandicap: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                MemberAvatar(
                    name = member.name,
                    gender = member.gender,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (member.nickname != null) {
                            Text(
                                text = "\"${member.nickname}\"",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GenderBadge(gender = member.gender)
                        ActiveStatusBadge(isActive = member.isActive)
                    }
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "선택됨",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (showHandicap && isSelected) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 60.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "핸디캡",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = if (handicap == 0) "" else handicap.toString(),
                        onValueChange = { text ->
                            val value = text.filter { it.isDigit() }.toIntOrNull() ?: 0
                            onHandicapChange(value.coerceIn(0, 50))
                        },
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        placeholder = { Text("0", style = MaterialTheme.typography.bodyMedium) }
                    )
                    Text(
                        text = "점/게임",
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
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

@Composable
private fun GenderBadge(
    gender: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (gender == "M") MaleColor.copy(alpha = 0.15f) else FemaleColor.copy(alpha = 0.15f)
    val textColor = if (gender == "M") MaleColor else FemaleColor
    val genderText = if (gender == "M") "남성" else "여성"

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = backgroundColor
    ) {
        Text(
            text = genderText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = textColor
            )
        )
    }
}

@Composable
private fun ActiveStatusBadge(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val statusColor = if (isActive) ActiveStatus else InactiveStatus
    val statusText = if (isActive) "활동" else "휴면"

    Badge(
        modifier = modifier,
        containerColor = statusColor,
        contentColor = Color.White
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
private fun SaveButton(
    enabled: Boolean,
    selectedCount: Int,
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
                text = "저장 (${selectedCount}명)",
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
                text = "회원 정보를 불러오는 중...",
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
                text = "👥",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp)
            )
            Text(
                text = "등록된 회원이 없습니다",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "먼저 회원을 등록해주세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticipantCheckScreenPreview() {
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
        ),
        Member(
            id = 3,
            name = "박민수",
            nickname = null,
            gender = "M",
            phoneNumber = "010-5555-6666",
            isActive = false,
            joinDate = LocalDate.of(2023, 11, 10),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    )

    BowlingClubTheme {
        ParticipantCheckContent(
            uiState = ParticipantCheckUiState(
                allMembers = sampleMembers,
                selectedMemberIds = setOf(1, 3)
            ),
            onToggleMember = {},
            onSelectAll = {},
            onDeselectAll = {},
            handicapMap = emptyMap(),
            tournamentHandicapEnabled = false,
            onHandicapChange = { _, _ -> },
            onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticipantCheckEmptyStatePreview() {
    BowlingClubTheme {
        ParticipantCheckContent(
            uiState = ParticipantCheckUiState(allMembers = emptyList()),
            onToggleMember = {},
            onSelectAll = {},
            onDeselectAll = {},
            handicapMap = emptyMap(),
            tournamentHandicapEnabled = false,
            onHandicapChange = { _, _ -> },
            onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MemberSelectionCardPreview() {
    BowlingClubTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MemberSelectionCard(
                    member = Member(
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
                    isSelected = true,
                    onToggle = {},
                    handicap = 0,
                    onHandicapChange = {},
                    showHandicap = false
                )
                MemberSelectionCard(
                    member = Member(
                        id = 2,
                        name = "이영희",
                        nickname = "스트라이크퀸",
                        gender = "F",
                        phoneNumber = "010-9876-5432",
                        isActive = false,
                        joinDate = LocalDate.of(2024, 2, 20),
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    ),
                    isSelected = false,
                    onToggle = {},
                    handicap = 0,
                    onHandicapChange = {},
                    showHandicap = false
                )
            }
        }
    }
}
