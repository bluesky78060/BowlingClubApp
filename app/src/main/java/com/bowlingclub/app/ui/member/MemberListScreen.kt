package com.bowlingclub.app.ui.member

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bowlingclub.app.data.local.entity.Member
import com.bowlingclub.app.ui.theme.ActiveStatus
import com.bowlingclub.app.ui.theme.BowlingTextStyles
import com.bowlingclub.app.ui.theme.FemaleColor
import com.bowlingclub.app.ui.theme.InactiveStatus
import com.bowlingclub.app.ui.theme.MaleColor
import com.bowlingclub.app.viewmodel.MemberListUiState
import com.bowlingclub.app.viewmodel.MemberListViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberListScreen(
    modifier: Modifier = Modifier,
    viewModel: MemberListViewModel = hiltViewModel(),
    onMemberClick: (Int) -> Unit = {},
    onAddMemberClick: () -> Unit = {}
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
                    Text(
                        text = "회원 관리",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMemberClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "새 회원 등록"
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        MemberListContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onSearchQueryChange = { viewModel.searchMembers(it) },
            onFilterChange = { viewModel.toggleFilter(it) },
            onMemberClick = onMemberClick
        )
    }
}

@Composable
private fun MemberListContent(
    modifier: Modifier = Modifier,
    uiState: MemberListUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (Boolean) -> Unit,
    onMemberClick: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SearchBar(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )

        FilterChipsRow(
            showActiveOnly = uiState.showActiveOnly,
            onFilterChange = onFilterChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.fillMaxSize())
            }
            uiState.members.isEmpty() -> {
                EmptyState(
                    showActiveOnly = uiState.showActiveOnly,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                MemberList(
                    members = uiState.members,
                    onMemberClick = onMemberClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = "이름 또는 닉네임으로 검색",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "검색",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "검색어 지우기",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipsRow(
    showActiveOnly: Boolean,
    onFilterChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = showActiveOnly,
            onClick = { onFilterChange(true) },
            label = {
                Text(
                    text = "활동 중",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            enabled = true,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = showActiveOnly,
                borderColor = if (showActiveOnly) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                selectedBorderColor = MaterialTheme.colorScheme.primary,
                borderWidth = 1.dp
            )
        )

        FilterChip(
            selected = !showActiveOnly,
            onClick = { onFilterChange(false) },
            label = {
                Text(
                    text = "전체",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            enabled = true,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = !showActiveOnly,
                borderColor = if (!showActiveOnly) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                selectedBorderColor = MaterialTheme.colorScheme.primary,
                borderWidth = 1.dp
            )
        )
    }
}

@Composable
private fun MemberList(
    members: List<Member>,
    onMemberClick: (Int) -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(members, key = { it.id }) { member ->
            MemberCard(
                member = member,
                onClick = { onMemberClick(member.id) }
            )
        }
    }
}

@Composable
private fun MemberCard(
    member: Member,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MemberAvatar(
                name = member.name,
                gender = member.gender,
                modifier = Modifier.size(56.dp)
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
                        style = BowlingTextStyles.MemberName,
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

                    Text(
                        text = "가입일: ${formatDate(member.joinDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            ActiveStatusBadge(isActive = member.isActive)
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
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 24.sp,
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
    showActiveOnly: Boolean,
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
                text = "📋",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp)
            )
            Text(
                text = if (showActiveOnly) "활동 중인 회원이 없습니다" else "등록된 회원이 없습니다",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (showActiveOnly) {
                    "전체 필터를 선택하거나 새 회원을 등록하세요"
                } else {
                    "오른쪽 하단 버튼을 눌러 새 회원을 등록하세요"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun MemberListScreenPreview() {
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

    MaterialTheme {
        MemberListContent(
            uiState = MemberListUiState(members = sampleMembers),
            onSearchQueryChange = {},
            onFilterChange = {},
            onMemberClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MemberListEmptyStatePreview() {
    MaterialTheme {
        MemberListContent(
            uiState = MemberListUiState(members = emptyList()),
            onSearchQueryChange = {},
            onFilterChange = {},
            onMemberClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MemberListLoadingStatePreview() {
    MaterialTheme {
        MemberListContent(
            uiState = MemberListUiState(isLoading = true),
            onSearchQueryChange = {},
            onFilterChange = {},
            onMemberClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MemberCardPreview() {
    MaterialTheme {
        Surface {
            MemberCard(
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
                onClick = {}
            )
        }
    }
}
