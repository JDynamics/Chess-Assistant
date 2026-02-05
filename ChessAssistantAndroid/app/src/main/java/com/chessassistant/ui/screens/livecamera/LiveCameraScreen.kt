package com.chessassistant.ui.screens.livecamera

import android.Manifest
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.chessassistant.R
import com.chessassistant.data.model.PlayerColor
import com.chessassistant.ui.theme.AccentColor
import com.chessassistant.ui.theme.BackgroundColor
import com.chessassistant.ui.theme.PanelColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

/**
 * Live camera screen for real-time chess board analysis.
 *
 * Features:
 * - Live camera preview
 * - Manual capture button
 * - Auto-analyze toggle (analyzes every few seconds)
 * - Flash toggle
 * - Front/back camera switch
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LiveCameraScreen(
    onNavigateBack: () -> Unit,
    viewModel: LiveCameraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    var previewView: PreviewView? by remember { mutableStateOf(null) }

    // Request permission on launch
    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.live_camera_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                // Flash toggle
                IconButton(onClick = { viewModel.toggleFlash() }) {
                    Icon(
                        imageVector = if (uiState.flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle flash",
                        tint = if (uiState.flashEnabled) AccentColor else Color.White
                    )
                }
                // Camera switch
                IconButton(onClick = { viewModel.switchCamera() }) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch camera",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
        )

        if (!cameraPermission.status.isGranted) {
            // Permission denied view
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Camera permission required",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { cameraPermission.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Camera Preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                    previewView = this
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Board overlay guide
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .border(2.dp, AccentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        )

                        // Analyzing indicator
                        if (uiState.isAnalyzing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = AccentColor)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Analyzing...",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Bind camera when preview is ready
                LaunchedEffect(previewView, uiState.useFrontCamera) {
                    previewView?.let { preview ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            cameraProvider = cameraProviderFuture.get()
                            bindCamera(
                                cameraProvider = cameraProvider!!,
                                previewView = preview,
                                lifecycleOwner = lifecycleOwner,
                                useFrontCamera = uiState.useFrontCamera,
                                flashEnabled = uiState.flashEnabled,
                                onFrameCaptured = { bitmap ->
                                    if (uiState.autoAnalyzeEnabled && !uiState.isAnalyzing) {
                                        viewModel.analyzeFrame(bitmap)
                                    }
                                }
                            )
                        }, ContextCompat.getMainExecutor(context))
                    }
                }

                DisposableEffect(Unit) {
                    onDispose {
                        cameraProvider?.unbindAll()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Playing as selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = uiState.playingAs == PlayerColor.WHITE,
                        onClick = { viewModel.setPlayingAs(PlayerColor.WHITE) },
                        label = { Text("Playing as White") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.playingAs == PlayerColor.BLACK,
                        onClick = { viewModel.setPlayingAs(PlayerColor.BLACK) },
                        label = { Text("Playing as Black") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Control buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Auto-analyze toggle
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { viewModel.toggleAutoAnalyze() },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (uiState.autoAnalyzeEnabled) AccentColor else PanelColor,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (uiState.autoAnalyzeEnabled) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Toggle auto-analyze",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.autoAnalyzeEnabled) "Auto ON" else "Auto OFF",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }

                    // Capture button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                previewView?.bitmap?.let { viewModel.captureAndAnalyze(it) }
                            },
                            enabled = !uiState.isAnalyzing,
                            modifier = Modifier
                                .size(72.dp)
                                .background(AccentColor, CircleShape)
                                .border(4.dp, Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Capture and analyze",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Analyze",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }

                    // Placeholder for symmetry
                    Box(modifier = Modifier.size(56.dp))
                }

                // Error message
                uiState.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error,
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF3D1F1F), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                }

                // Result
                uiState.lastResult?.let { result ->
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Best Move",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = result.formattedMove ?: "No move found",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            result.fen?.let { fen ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = fen.take(50) + if (fen.length > 50) "..." else "",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun bindCamera(
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    useFrontCamera: Boolean,
    flashEnabled: Boolean,
    onFrameCaptured: (Bitmap) -> Unit
) {
    cameraProvider.unbindAll()

    val cameraSelector = if (useFrontCamera) {
        CameraSelector.DEFAULT_FRONT_CAMERA
    } else {
        CameraSelector.DEFAULT_BACK_CAMERA
    }

    val preview = Preview.Builder()
        .build()
        .also {
            it.surfaceProvider = previewView.surfaceProvider
        }

    val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    val executor = Executors.newSingleThreadExecutor()
    var lastAnalysisTime = 0L
    val analysisIntervalMs = 3000L // Analyze every 3 seconds when auto mode is on

    imageAnalysis.setAnalyzer(executor) { imageProxy ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalysisTime >= analysisIntervalMs) {
            lastAnalysisTime = currentTime
            val bitmap = imageProxy.toBitmapSafe()
            bitmap?.let { onFrameCaptured(it) }
        }
        imageProxy.close()
    }

    try {
        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis
        )

        camera.cameraControl.enableTorch(flashEnabled)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun ImageProxy.toBitmapSafe(): Bitmap? {
    return try {
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }
}
