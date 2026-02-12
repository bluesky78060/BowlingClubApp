package com.bowlingclub.app.ui.member

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bowlingclub.app.util.ImagePickerUtil
import com.bowlingclub.app.viewmodel.MemberEditViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberEditScreen(
    modifier: Modifier = Modifier,
    viewModel: MemberEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDatePicker by remember { mutableStateOf(false) }
    var showBirthDatePicker by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = ImagePickerUtil.saveImageToInternalStorage(context, it)
            savedPath?.let { path ->
                viewModel.updateProfileImage(path)
            }
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            scope.launch {
                snackbarHostState.showSnackbar(errorMessage)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (viewModel.uiState.value.name.isEmpty() &&
                                  !viewModel.uiState.value.isLoading) {
                            "새 회원 등록"
                        } else if (viewModel.uiState.value.isLoading) {
                            "회원 정보 불러오는 중..."
                        } else {
                            "회원 수정"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileImageSection(
                    profileImagePath = uiState.profileImagePath,
                    onImageClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                FormSection(
                    name = uiState.name,
                    onNameChange = viewModel::updateName,
                    nickname = uiState.nickname,
                    onNicknameChange = viewModel::updateNickname,
                    gender = uiState.gender,
                    onGenderChange = viewModel::updateGender,
                    birthDate = uiState.birthDate,
                    onBirthDateClick = { showBirthDatePicker = true },
                    phoneNumber = uiState.phoneNumber,
                    onPhoneNumberChange = viewModel::updatePhoneNumber,
                    address = uiState.address,
                    onAddressChange = viewModel::updateAddress,
                    joinDate = uiState.joinDate,
                    onJoinDateClick = { showDatePicker = true },
                    memo = uiState.memo,
                    onMemoChange = viewModel::updateMemo,
                    isLoading = uiState.isLoading
                )

                Spacer(modifier = Modifier.height(32.dp))

                SaveButton(
                    onClick = viewModel::saveMember,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showBirthDatePicker) {
                DatePickerModal(
                    initialDate = uiState.birthDate ?: LocalDate.of(1990, 1, 1),
                    onDateSelected = { selectedDate ->
                        viewModel.updateBirthDate(selectedDate)
                        showBirthDatePicker = false
                    },
                    onDismiss = { showBirthDatePicker = false }
                )
            }

            if (showDatePicker) {
                DatePickerModal(
                    initialDate = uiState.joinDate,
                    onDateSelected = { selectedDate ->
                        viewModel.updateJoinDate(selectedDate)
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }
        }
    }
}

@Composable
private fun ProfileImageSection(
    profileImagePath: String?,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(
        targetValue = if (profileImagePath != null) 12.dp else 6.dp,
        animationSpec = tween(durationMillis = 300),
        label = "elevation"
    )

    Box(
        modifier = modifier
            .size(160.dp)
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onImageClick),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (profileImagePath != null) {
                    val imageFile = File(profileImagePath)
                    if (imageFile.exists()) {
                        AsyncImage(
                            model = imageFile,
                            contentDescription = "프로필 이미지",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        DefaultProfileIcon()
                    }
                } else {
                    DefaultProfileIcon()
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            )
                        )
                )

                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "사진 추가",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(6.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun DefaultProfileIcon() {
    Icon(
        imageVector = Icons.Default.Person,
        contentDescription = "기본 프로필",
        modifier = Modifier.size(80.dp),
        tint = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@Composable
private fun FormSection(
    name: String,
    onNameChange: (String) -> Unit,
    nickname: String,
    onNicknameChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    birthDate: LocalDate?,
    onBirthDateClick: () -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    joinDate: LocalDate,
    onJoinDateClick: () -> Unit,
    memo: String,
    onMemoChange: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        StyledOutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = "이름 *",
            enabled = !isLoading,
            isRequired = true
        )

        StyledOutlinedTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            label = "닉네임",
            enabled = !isLoading
        )

        GenderSelector(
            selectedGender = gender,
            onGenderChange = onGenderChange,
            enabled = !isLoading
        )

        NullableDateField(
            date = birthDate,
            label = "생년월일",
            onClick = onBirthDateClick,
            enabled = !isLoading
        )

        StyledOutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = "연락처",
            enabled = !isLoading,
            keyboardType = KeyboardType.Phone
        )

        StyledOutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = "주소",
            enabled = !isLoading,
            minLines = 1,
            maxLines = 3
        )

        DateField(
            date = joinDate,
            onClick = onJoinDateClick,
            enabled = !isLoading
        )

        StyledOutlinedTextField(
            value = memo,
            onValueChange = onMemoChange,
            label = "메모",
            enabled = !isLoading,
            minLines = 3,
            maxLines = 5
        )
    }
}

@Composable
private fun StyledOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isRequired: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = maxLines == 1,
        minLines = minLines,
        maxLines = maxLines,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = if (isRequired && value.isEmpty()) FontWeight.Normal else FontWeight.Medium
        )
    )
}

@Composable
private fun GenderSelector(
    selectedGender: String,
    onGenderChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "성별",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GenderOption(
                text = "남성",
                selected = selectedGender == "M",
                onClick = { onGenderChange("M") },
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )

            GenderOption(
                text = "여성",
                selected = selectedGender == "F",
                onClick = { onGenderChange("F") },
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GenderOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(56.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium
            )
            .clickable(enabled = enabled, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                enabled = enabled,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.outline
                )
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun NullableDateField(
    date: LocalDate?,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")

    OutlinedTextField(
        value = date?.format(dateFormatter) ?: "",
        onValueChange = {},
        label = { Text(label) },
        placeholder = { Text("선택해주세요") },
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        enabled = false,
        readOnly = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "날짜 선택",
                tint = if (enabled) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        },
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun DateField(
    date: LocalDate,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")

    OutlinedTextField(
        value = date.format(dateFormatter),
        onValueChange = {},
        label = { Text("가입일") },
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        enabled = false,
        readOnly = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "날짜 선택",
                tint = if (enabled) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        },
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                }
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun SaveButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = !isLoading,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            disabledElevation = 0.dp
        )
    ) {
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
        }

        Text(
            text = if (isLoading) "저장 중..." else "저장",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MemberEditScreenPreview() {
    MaterialTheme {
        Surface {
            MemberEditScreen()
        }
    }
}
