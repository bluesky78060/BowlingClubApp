package com.bowlingclub.app.ui.member

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bowlingclub.app.data.local.entity.Member
import com.bowlingclub.app.ui.theme.ActiveStatus
import com.bowlingclub.app.ui.theme.BowlingClubTheme
import com.bowlingclub.app.ui.theme.FemaleColor
import com.bowlingclub.app.ui.theme.InactiveStatus
import com.bowlingclub.app.ui.theme.MaleColor
import com.bowlingclub.app.viewmodel.MemberDetailViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MemberDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: MemberDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MemberDetailTopBar(
                memberName = uiState.member?.name ?: "",
                onNavigateBack = onNavigateBack,
                onEdit = { uiState.member?.let { onNavigateToEdit(it.id) } },
                onMenuClick = { showMenu = true },
                showMenu = showMenu,
                onDismissMenu = { showMenu = false },
                onDelete = { showDeleteDialog = true }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.padding(paddingValues))
            }
            uiState.member != null -> {
                MemberDetailContent(
                    member = uiState.member!!,
                    onToggleActiveStatus = viewModel::toggleActiveStatus,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                ErrorState(
                    message = "회원 정보를 불러올 수 없습니다",
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = {
                viewModel.deleteMember()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberDetailTopBar(
    memberName: String,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    onMenuClick: () -> Unit,
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = memberName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기"
                )
            }
        },
        actions = {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "수정"
                )
            }
            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "더보기"
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(
                        text = { Text("삭제") },
                        onClick = {
                            onDelete()
                            onDismissMenu()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun MemberDetailContent(
    member: Member,
    onToggleActiveStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ProfileSection(member = member)

        Spacer(modifier = Modifier.height(24.dp))

        DetailInfoCard(member = member)

        Spacer(modifier = Modifier.height(24.dp))

        ActiveStatusToggleButton(
            isActive = member.isActive,
            onClick = onToggleActiveStatus,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileSection(member: Member) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileImage(name = member.name, imagePath = member.profileImagePath)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = member.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (member.nickname != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = member.nickname,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AssistChip(
            onClick = { },
            label = {
                Text(
                    text = if (member.isActive) "활동" else "휴면",
                    fontWeight = FontWeight.SemiBold
                )
            },
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (member.isActive) ActiveStatus else InactiveStatus)
                )
            }
        )
    }
}

@Composable
private fun ProfileImage(
    name: String,
    imagePath: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(100.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 4.dp
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (imagePath == null) {
                Text(
                    text = name.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun DetailInfoCard(member: Member) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "상세 정보",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            InfoRow(
                icon = if (member.gender == "M") Icons.Default.Male else Icons.Default.Female,
                label = "성별",
                value = if (member.gender == "M") "남성" else "여성",
                iconTint = if (member.gender == "M") MaleColor else FemaleColor
            )

            if (member.birthDate != null) {
                InfoRow(
                    icon = Icons.Default.Cake,
                    label = "생년월일",
                    value = member.birthDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                )
            }

            if (member.phoneNumber != null) {
                InfoRow(
                    icon = Icons.Default.Phone,
                    label = "연락처",
                    value = member.phoneNumber
                )
            }

            if (member.address != null) {
                InfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "주소",
                    value = member.address,
                    isMultiLine = true
                )
            }

            InfoRow(
                icon = Icons.Default.CalendarToday,
                label = "가입일",
                value = member.joinDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
            )

            if (member.memo != null) {
                InfoRow(
                    icon = Icons.Default.Notes,
                    label = "메모",
                    value = member.memo,
                    isMultiLine = true
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
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    isMultiLine: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = if (isMultiLine) Alignment.Top else Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = iconTint
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ActiveStatusToggleButton(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        exit = fadeOut()
    ) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(52.dp)
        ) {
            Text(
                text = if (isActive) "휴면으로 전환" else "활동으로 전환",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "회원 삭제",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "정말 이 회원을 삭제하시겠습니까?\n삭제된 회원 정보는 복구할 수 없습니다.",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("삭제", color = MaterialTheme.colorScheme.error)
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
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MemberDetailScreenPreview() {
    BowlingClubTheme {
        MemberDetailContent(
            member = Member(
                id = 1,
                name = "김철수",
                nickname = "볼링왕",
                gender = "M",
                phoneNumber = "010-1234-5678",
                profileImagePath = null,
                isActive = true,
                joinDate = LocalDate.of(2024, 1, 15),
                memo = "매주 화요일 저녁 참석\n고득점 회원",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            onToggleActiveStatus = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MemberDetailScreenInactivePreview() {
    BowlingClubTheme {
        MemberDetailContent(
            member = Member(
                id = 2,
                name = "박영희",
                nickname = null,
                gender = "F",
                phoneNumber = null,
                profileImagePath = null,
                isActive = false,
                joinDate = LocalDate.of(2023, 6, 10),
                memo = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            onToggleActiveStatus = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MemberDetailScreenDarkPreview() {
    BowlingClubTheme {
        MemberDetailContent(
            member = Member(
                id = 1,
                name = "김철수",
                nickname = "볼링왕",
                gender = "M",
                phoneNumber = "010-1234-5678",
                profileImagePath = null,
                isActive = true,
                joinDate = LocalDate.of(2024, 1, 15),
                memo = "매주 화요일 저녁 참석",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            onToggleActiveStatus = {}
        )
    }
}
