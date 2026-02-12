package com.bowlingclub.app.ui.score

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bowlingclub.app.ui.theme.BowlingClubTheme
import com.bowlingclub.app.util.CameraPreview
import com.bowlingclub.app.util.capturePhoto
import com.bowlingclub.app.viewmodel.OcrViewModel
import com.bowlingclub.app.viewmodel.ScoreInputViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreInputTabScreen(
    modifier: Modifier = Modifier,
    scoreInputViewModel: ScoreInputViewModel = hiltViewModel(),
    ocrViewModel: OcrViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val manualUiState by scoreInputViewModel.uiState.collectAsState()
    val ocrUiState by ocrViewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var hasCameraPermission by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { ocrViewModel.processImage(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(manualUiState.error) {
        manualUiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
        }
    }

    LaunchedEffect(ocrUiState.error) {
        ocrUiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
            ocrViewModel.clearError()
        }
    }

    LaunchedEffect(manualUiState.isSaved, ocrUiState.isSaved) {
        if (manualUiState.isSaved || ocrUiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "점수 입력",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "카메라 (OCR)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    },
                    modifier = Modifier.height(64.dp)
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "수동 입력",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    },
                    modifier = Modifier.height(64.dp)
                )
            }

            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> {
                        OcrTabContent(
                            hasCameraPermission = hasCameraPermission,
                            ocrUiState = ocrUiState,
                            onImageCaptureReady = { imageCapture = it },
                            onCaptureClick = {
                                scope.launch {
                                    imageCapture?.let { capture ->
                                        val result = capturePhoto(context, capture)
                                        result.onSuccess { uri ->
                                            ocrViewModel.processImage(uri)
                                        }.onFailure { error ->
                                            snackbarHostState.showSnackbar(
                                                "촬영 실패: ${error.message}"
                                            )
                                        }
                                    }
                                }
                            },
                            onGalleryClick = { galleryLauncher.launch("image/*") },
                            onRetakeClick = { ocrViewModel.resetOcr() },
                            onSaveClick = { ocrViewModel.saveScores() },
                            onScoreChange = { playerName, gameIndex, score ->
                                ocrViewModel.updateScore(playerName, gameIndex, score)
                            },
                            onMemberMappingChange = { playerName, memberId ->
                                ocrViewModel.updateMemberMapping(playerName, memberId)
                            }
                        )
                    }
                    1 -> {
                        ManualTabContent(
                            uiState = manualUiState,
                            onScoreChange = { memberId, gameNumber, score ->
                                scoreInputViewModel.updateScore(memberId, gameNumber, score)
                            },
                            onSaveClick = { scoreInputViewModel.saveAllScores() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OcrTabContent(
    hasCameraPermission: Boolean,
    ocrUiState: com.bowlingclub.app.viewmodel.OcrUiState,
    onImageCaptureReady: (ImageCapture) -> Unit,
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRetakeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onScoreChange: (String, Int, Int) -> Unit,
    onMemberMappingChange: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            ocrUiState.isProcessing -> {
                ProcessingOverlay(
                    modifier = Modifier.fillMaxSize()
                )
            }
            ocrUiState.parseResult != null && ocrUiState.editableScores.isNotEmpty() -> {
                OcrPreviewContent(
                    modifier = Modifier.fillMaxSize(),
                    uiState = ocrUiState,
                    onScoreChange = onScoreChange,
                    onMemberMappingChange = onMemberMappingChange,
                    onSaveClick = onSaveClick,
                    onRetakeClick = onRetakeClick
                )
            }
            hasCameraPermission -> {
                CameraPreviewWithCapture(
                    onImageCaptureReady = onImageCaptureReady,
                    onCaptureClick = onCaptureClick,
                    onGalleryClick = onGalleryClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                GalleryOnlyContent(
                    onGalleryClick = onGalleryClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun CameraPreviewWithCapture(
    onImageCaptureReady: (ImageCapture) -> Unit,
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onImageCaptureReady = onImageCaptureReady
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "스코어 보드를 화면에 맞춰주세요",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = onGalleryClick,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    containerColor = Color.White.copy(alpha = 0.3f),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "갤러리",
                        modifier = Modifier.size(28.dp)
                    )
                }

                FloatingActionButton(
                    onClick = onCaptureClick,
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "촬영",
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Spacer for symmetry
                Spacer(modifier = Modifier.size(56.dp))
            }
        }
    }
}

@Composable
private fun ProcessingOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(56.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 5.dp
                )
                Text(
                    text = "점수 인식 중...",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "잠시만 기다려주세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GalleryOnlyContent(
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "카메라 권한이 없습니다",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "갤러리에서 스코어보드 사진을 선택하거나\n설정에서 카메라 권한을 허용해주세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onGalleryClick,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("갤러리에서 선택")
            }
        }
    }
}


@Composable
private fun ManualTabContent(
    uiState: com.bowlingclub.app.viewmodel.ScoreInputUiState,
    onScoreChange: (Int, Int, Int) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScoreInputContent(
        modifier = modifier,
        uiState = uiState,
        onScoreChange = onScoreChange,
        onSaveClick = onSaveClick
    )
}

@Preview(showBackground = true)
@Composable
private fun ScoreInputTabScreenPreview() {
    BowlingClubTheme {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Score Input Tab Screen",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProcessingOverlayPreview() {
    BowlingClubTheme {
        Surface {
            ProcessingOverlay(modifier = Modifier.fillMaxSize())
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GalleryOnlyPreview() {
    BowlingClubTheme {
        Surface {
            GalleryOnlyContent(
                onGalleryClick = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
