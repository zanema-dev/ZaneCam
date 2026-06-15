package com.example.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.CameraSettings
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CameraScreen(viewModel: CameraViewModel) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            hasCameraPermission = permissions[Manifest.permission.CAMERA] == true &&
                    permissions[Manifest.permission.RECORD_AUDIO] == true
        }
    )

    // Intro splash tracker
    var showIntro by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2200) // 2.2 seconds elegant intro
        showIntro = false
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PureBlack)
        ) {
            when {
                showIntro -> {
                    CinematicIntroScreen()
                }
                !hasCameraPermission -> {
                    PermissionRequestView {
                        launcher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                            )
                        )
                    }
                }
                else -> {
                    MainCameraLayout(viewModel)
                }
            }
        }
    }
}

@Composable
fun CinematicIntroScreen() {
    var animTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        animTrigger = true
    }

    val scale by animateFloatAsState(
        targetValue = if (animTrigger) 1.05f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "LogoScale"
    )

    val opacity by animateFloatAsState(
        targetValue = if (animTrigger) 1.0f else 0.0f,
        animationSpec = tween(1200, easing = LinearOutSlowInEasing),
        label = "LogoOpacity"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(CineDarkRed.copy(alpha = 0.45f), PureBlack),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Camera Shutter Aperture simulation
            Canvas(
                modifier = Modifier
                    .size(85.dp)
                    .rotate(if (animTrigger) 180f else 0f)
                    .pointerInput(Unit) {}
            ) {
                drawCircle(
                    color = CineRed,
                    radius = size.width / 2.3f,
                    style = Stroke(width = 3.dp.toPx())
                )
                // Draw shutter leaves
                val radius = size.width / 2.3f
                for (i in 0 until 6) {
                    val angle = (i * 60f) * Math.PI.toFloat() / 180f
                    val x = center.x + radius * kotlin.math.cos(angle)
                    val y = center.y + radius * kotlin.math.sin(angle)
                    drawLine(
                        color = CineRed.copy(alpha = 0.5f),
                        start = center,
                        end = Offset(x, y),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ZANECAM",
                fontSize = 38.sp,
                color = LightGray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 12.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.rotate(0f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SOCIETY OF DIGITAL CINEMA SYSTEMS",
                fontSize = 11.sp,
                color = MediumGray,
                fontWeight = FontWeight.Light,
                letterSpacing = 4.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.rotate(0f)
            )
        }
    }
}

@Composable
fun PermissionRequestView(onRequest: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VideocamOff,
                contentDescription = "Camera Bloqueada",
                tint = CineRed,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "LENS OFFLINE / AUD SIGNAL MISSING",
                color = LightGray,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ZaneCam precisa de permissão de Câmera e Microfone para gravação cinematográfica em alta fidelidade.",
                color = MediumGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CineRed,
                    contentColor = PureBlack
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    text = "INICIALIZAR SENSORES",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun MainCameraLayout(viewModel: CameraViewModel) {
    val settings by viewModel.settings.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingDuration by viewModel.recordingDuration.collectAsState()
    val zoomRatio by viewModel.zoomRatio.collectAsState()
    val uiLocked by viewModel.uiLocked.collectAsState()
    val lockHoldProgress by viewModel.lockHoldProgress.collectAsState()
    val horizonTiltAngle by viewModel.horizonTiltAngle.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val storageMinutesLeft by viewModel.storageMinutesLeft.collectAsState()
    val histogramData by viewModel.histogramData.collectAsState()

    // Slide-out state panels
    var showFpsDialog by remember { mutableStateOf(false) }
    var showResDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // 1. Live Camera Preview
        CameraPreview(
            zoomRatio = zoomRatio,
            aeAfLocked = settings.aeAfLocked,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Proximity Focus Peaking simulated filter overlay
        if (settings.focusPeakingEnabled) {
            SimulatedFocusPeakingOverlay()
        }

        // 3. Highlight Zebra Stripes simulated filter overlay
        if (settings.zebraPatternEnabled) {
            SimulatedZebraStripesOverlay()
        }

        // 4. Cinematic 3x3 Grid
        if (settings.gridVisible) {
            CinematicGridOverlay()
        }

        // 5. Simulated horizon Leveler
        if (settings.levelerEnabled) {
            HorizonLevelerOverlay(tiltAngle = horizonTiltAngle)
        }

        // 6. Cinematic aspect ratio letterbox bars (2.39:1 Aspect ratio matte)
        // With red letterbox outlines when recording is active
        CinematicLetterboxBars(isRecording = isRecording)

        // 7. Dynamic live histogram view
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 24.dp, top = 80.dp)
        ) {
            LiveHistogramChart(histogramData = histogramData)
        }

        // 8. Custom continuous zoom scale overlay
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
        ) {
            ZoomSliderIndicator(zoomRatio = zoomRatio)
        }

        // 9. Interactive Heads-Up Display (Metrics & Controllers)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP BAR (Metrics HUD info)
            HeaderMetadataHud(
                batteryLevel = batteryLevel,
                storageMins = storageMinutesLeft,
                fps = settings.fps,
                resolution = settings.resolutionValue,
                isRecording = isRecording,
                durationSeconds = recordingDuration,
                onFpsClick = { if (!uiLocked) showFpsDialog = true },
                onResClick = { if (!uiLocked) showResDialog = true }
            )

            // MIDDLE SPACE - Locked state warning overlay
            if (uiLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(PureBlack.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = CineRed,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "INTERFACE BLOQUEADA",
                            color = LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Mantenha pressionado o cadeado por 2s",
                            color = MediumGray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // BOTTOM CONTROL ZONE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureBlack.copy(alpha = 0.85f))
                    .padding(bottom = 16.dp)
            ) {
                // Sliders Shelf (When unlocked)
                if (!uiLocked) {
                    ManualSettingsSliders(
                        settings = settings,
                        onIsoChange = { viewModel.updateIso(it) },
                        onEvChange = { viewModel.updateEv(it) },
                        onFocusChange = { viewModel.updateManualFocus(it) }
                    )
                }

                // Main control action shelf with Shutter, Toggles & holding lock button
                ControlActionShelf(
                    settings = settings,
                    isRecording = isRecording,
                    uiLocked = uiLocked,
                    lockHoldProgress = lockHoldProgress,
                    onToggleAeLock = { viewModel.toggleAeAf() },
                    onToggleGrid = { viewModel.toggleGrid() },
                    onToggleZebra = { viewModel.toggleZebra() },
                    onTogglePeaking = { viewModel.toggleFocusPeaking() },
                    onToggleLeveler = { viewModel.toggleLeveler() },
                    onToggleProfile = { 
                        val nextProfile = if (settings.colorProfile == "REC709") "LOG" else "REC709"
                        viewModel.updateColorProfile(nextProfile)
                    },
                    onShutterClick = { viewModel.toggleRecording() },
                    onLockClick = { viewModel.toggleLock() },
                    onUnlockHoldStart = { viewModel.startUnlockingHold() },
                    onUnlockHoldCancel = { viewModel.cancelUnlockingHold() }
                )
            }
        }

        // FPS Selection Overlay BottomSheet style
        if (showFpsDialog) {
            InteractiveConfigSelector(
                title = "TAXA DE QUADROS (FPS)",
                options = listOf("24", "25", "30", "50", "60", "120"),
                selectedOption = settings.fps.toString(),
                onSelect = {
                    viewModel.updateFps(it.toInt())
                    showFpsDialog = false
                },
                onDismiss = { showFpsDialog = false }
            )
        }

        // Resolution Selection Overlay BottomSheet style
        if (showResDialog) {
            InteractiveConfigSelector(
                title = "RESOLUÇÃO CINEMATOGRÁFICA",
                options = listOf("720p", "1080p", "2K", "4K"),
                selectedOption = settings.resolutionValue,
                onSelect = {
                    viewModel.updateResolution(it)
                    showResDialog = false
                },
                onDismiss = { showResDialog = false }
            )
        }
    }
}

@Composable
fun CinematicLetterboxBars(isRecording: Boolean) {
    // Top and bottom 2.39:1 cinema bars
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar matte
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(PureBlack)
        ) {
            // Draw cinema border marker when recording is active
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(CineRed)
                        .align(Alignment.BottomCenter)
                )
            }
        }

        // Viewable safe core (aspect ratio 2.39:1 content)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.39f)
        )

        // Bottom Bar matte
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(PureBlack)
        ) {
            // Draw cinema border marker when recording is active
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(CineRed)
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
fun CinematicGridOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Horizontal Grid guidelines
        drawLine(
            color = LightGray.copy(alpha = 0.2f),
            start = Offset(0f, height / 3f),
            end = Offset(width, height / 3f),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
        drawLine(
            color = LightGray.copy(alpha = 0.2f),
            start = Offset(0f, 2 * height / 3f),
            end = Offset(width, 2 * height / 3f),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        // Vertical Grid guidelines
        drawLine(
            color = LightGray.copy(alpha = 0.2f),
            start = Offset(width / 3f, 0f),
            end = Offset(width / 3f, height),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
        drawLine(
            color = LightGray.copy(alpha = 0.2f),
            start = Offset(2 * width / 3f, 0f),
            end = Offset(2 * width / 3f, height),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}

@Composable
fun HorizonLevelerOverlay(tiltAngle: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)
        val levelLength = 200f

        val isLeveled = tiltAngle in -1.3f..1.3f
        val color = if (isLeveled) FocusGreen else LightGray.copy(alpha = 0.5f)

        // Draw central crosshair level
        drawCircle(
            color = color,
            radius = 6f,
            center = center,
            style = Stroke(width = 1.5f.dp.toPx())
        )

        // Draw horizon lines rotating with accelerometer
        withTransform({
            rotate(tiltAngle, center)
        }) {
            // Draw left bar
            drawLine(
                color = color,
                start = Offset(center.x - levelLength, center.y),
                end = Offset(center.x - 40f, center.y),
                strokeWidth = 2.dp.toPx()
            )
            // Draw right bar
            drawLine(
                color = color,
                start = Offset(center.x + 40f, center.y),
                end = Offset(center.x + levelLength, center.y),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Draw static level shelf notches
        drawLine(
            color = if (isLeveled) FocusGreen else CineRed.copy(alpha = 0.4f),
            start = Offset(center.x - 30f, center.y - 12f),
            end = Offset(center.x - 30f, center.y + 12f),
            strokeWidth = 1f.dp.toPx()
        )
        drawLine(
            color = if (isLeveled) FocusGreen else CineRed.copy(alpha = 0.4f),
            start = Offset(center.x + 30f, center.y - 12f),
            end = Offset(center.x + 30f, center.y + 12f),
            strokeWidth = 1f.dp.toPx()
        )
    }
}

@Composable
fun SimulatedFocusPeakingOverlay() {
    // Draw high-contrast focal highlights representing real Focus Peaking in green
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Place a group of simulated focal edge points centered around middle-third of the frame
        val density = 35
        val rng = java.util.Random(42) // Constant seed for stable position flickering
        for (i in 0 until density) {
            val rx = center.x + (rng.nextGaussian() * (width / 5f)).toFloat()
            val ry = center.y + (rng.nextGaussian() * (height / 8f)).toFloat()
            
            // Randomly draw tiny neon edge indicators
            drawCircle(
                color = FocusGreen.copy(alpha = 0.8f),
                radius = 1.5f.dp.toPx(),
                center = Offset(rx, ry)
            )
        }
    }
}

@Composable
fun SimulatedZebraStripesOverlay() {
    // Draw diagonal warning stripes in high-brightness spots (such as upper quadrants)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val barSize = 60f

        // Draw 3 exposure zebra warning boxes at the top left/right edges
        val stripePaint = Color(0xFFFFD700) // Gold Warning
        val stripeGap = 15f
        
        // Draw Zebra highlights on top sector (simulated sky exposure)
        for (offset in 0..10) {
            val startX = (width * 0.7f) + offset * stripeGap
            drawLine(
                color = stripePaint.copy(alpha = 0.35f),
                start = Offset(startX, height * 0.15f),
                end = Offset(startX - 20f, (height * 0.15f) + 30f),
                strokeWidth = 3f
            )
        }
    }
}

@Composable
fun LiveHistogramChart(histogramData: FloatArray) {
    if (histogramData.isEmpty()) return
    Canvas(
        modifier = Modifier
            .size(width = 110.dp, height = 55.dp)
            .background(PureBlack.copy(alpha = 0.65f), RoundedCornerShape(2.dp))
            .border(0.5.dp, MediumGray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            .padding(4.dp)
    ) {
        val path = androidx.compose.ui.graphics.Path()

        val itemWidth = size.width / (histogramData.size - 1)
        val maxHeight = size.height

        path.moveTo(0f, maxHeight)
        for (i in histogramData.indices) {
            val x = i * itemWidth
            val y = maxHeight - (histogramData[i] / 100f * maxHeight).coerceIn(0f, maxHeight)
            path.lineTo(x, y)
        }
        path.lineTo(size.width, maxHeight)
        path.close()

        // Draw flat diagnostic area wave
        drawPath(
            path = path,
            color = CineRed.copy(alpha = 0.35f)
        )

        // Draw thin outlines
        val strokePath = androidx.compose.ui.graphics.Path()
        strokePath.moveTo(0f, maxHeight)
        for (i in histogramData.indices) {
            val x = i * itemWidth
            val y = maxHeight - (histogramData[i] / 100f * maxHeight).coerceIn(0f, maxHeight)
            if (i == 0) strokePath.moveTo(x, y) else strokePath.lineTo(x, y)
        }
        drawPath(
            path = strokePath,
            color = CineRed,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

@Composable
fun ZoomSliderIndicator(zoomRatio: Float) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .background(PureBlack.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
            .border(0.5.dp, CineRed.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "ZOOM",
            fontSize = 8.sp,
            color = CineRed,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = String.format("%.1fx", zoomRatio),
            fontSize = 13.sp,
            color = LightGray,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Micro scale ticks
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .drawBehind {
                    val steps = 5
                    val stepHeight = size.height / steps
                    for (i in 0..steps) {
                        val isMiddle = i == steps / 2
                        drawLine(
                            color = if (isMiddle) CineRed else LightGray.copy(alpha = 0.3f),
                            start = Offset(if (isMiddle) 00f else 6f, i * stepHeight),
                            end = Offset(size.width, i * stepHeight),
                            strokeWidth = 1f.dp.toPx()
                        )
                    }
                }
        )
    }
}

@Composable
fun HeaderMetadataHud(
    batteryLevel: Int,
    storageMins: Int,
    fps: Int,
    resolution: String,
    isRecording: Boolean,
    durationSeconds: Int,
    onFpsClick: () -> Unit,
    onResClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PureBlack.copy(alpha = 0.9f), PureBlack.copy(alpha = 0.0f))
                )
            )
            .padding(start = 24.dp, end = 24.dp, top = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dynamic Recording Ticker & Timecode Center (Standard Hollywood Format)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            val tickerPulseTransition = rememberInfiniteTransition(label = "pulse_rec")
            val blinkAlpha by tickerPulseTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 0.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "rec_alpha"
            )

            if (isRecording) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CineRed.copy(alpha = blinkAlpha))
                )
                Spacer(modifier = Modifier.width(6.dp))
                
                // Format duration to Hours:Mins:Secs:Frames
                val hours = durationSeconds / 3600
                val mins = (durationSeconds % 3600) / 60
                val secs = durationSeconds % 60
                val frames = (System.currentTimeMillis() / 41) % 24 // Dynamic running cinema frames

                Text(
                    text = String.format("%02d:%02d:%02d:%02d", hours, mins, secs, frames),
                    color = CineRed,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MediumGray)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "STANDBY",
                    color = MediumGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        // Mid Specs (Format & framerate - click to toggle)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = resolution,
                color = LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .border(0.5.dp, CineRed.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                    .clickable { onResClick() }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )

            Text(
                text = "${fps}FPS",
                color = LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .border(0.5.dp, CineRed.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                    .clickable { onFpsClick() }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        // Storage remaining space & battery info
        Row(
            modifier = Modifier.weight(1f).offset(x = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Armazenamento",
                tint = LightGray.copy(alpha = 0.7f),
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${storageMins}M",
                color = LightGray.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                imageVector = if (batteryLevel > 50) Icons.Default.BatteryFull else Icons.Default.BatteryAlert,
                contentDescription = "Bateria",
                tint = if (batteryLevel < 20) CineRed else GoldAccent,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$batteryLevel%",
                color = LightGray.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ManualSettingsSliders(
    settings: CameraSettings,
    onIsoChange: (Int) -> Unit,
    onEvChange: (Float) -> Unit,
    onFocusChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Slider 1: ISO control
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "ISO",
                color = LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(36.dp)
            )

            val isoSteps = listOf(100, 200, 320, 400, 500, 640, 800, 1600, 3200, 6400)
            val currentStepIndex = isoSteps.indexOf(settings.iso).coerceAtLeast(0)

            Slider(
                value = currentStepIndex.toFloat(),
                onValueChange = { index ->
                    val chosen = isoSteps[index.toInt().coerceIn(0, isoSteps.size - 1)]
                    onIsoChange(chosen)
                },
                valueRange = 0f..(isoSteps.size - 1).toFloat(),
                steps = isoSteps.size - 2,
                colors = SliderDefaults.colors(
                    activeTickColor = CineRed,
                    inactiveTickColor = MediumGray,
                    activeTrackColor = CineRed,
                    thumbColor = CineRed
                ),
                modifier = Modifier.weight(1f).height(12.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "${settings.iso}",
                color = LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(40.dp),
                textAlign = Alignment.End.let { TextAlign.End }
            )
        }

        // Slider 2: EV (Exposure Level)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "EV",
                color = LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(36.dp)
            )

            Slider(
                value = settings.ev,
                onValueChange = { onEvChange(it) },
                valueRange = -3.0f..3.0f,
                colors = SliderDefaults.colors(
                    activeTrackColor = CineRed,
                    thumbColor = CineRed
                ),
                modifier = Modifier.weight(1f).height(12.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = String.format("%s%.1f", if (settings.ev >= 0) "+" else "", settings.ev),
                color = LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End
            )
        }

        // Slider 3: Foco Manual (If updated, automatically fires Focus Peaking visual hints)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.width(36.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "FOC",
                    color = if (settings.focusPeakingEnabled) FocusGreen else LightGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Slider(
                value = settings.manualFocus,
                onValueChange = { onFocusChange(it) },
                valueRange = 0.0f..1.0f,
                colors = SliderDefaults.colors(
                    activeTrackColor = if (settings.focusPeakingEnabled) FocusGreen else CineRed,
                    thumbColor = if (settings.focusPeakingEnabled) FocusGreen else CineRed
                ),
                modifier = Modifier.weight(1f).height(12.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = when {
                    settings.manualFocus < 0.15f -> "MACRO"
                    settings.manualFocus > 0.85f -> "INF"
                    else -> String.format("%.2fm", settings.manualFocus * 10f)
                },
                color = if (settings.focusPeakingEnabled) FocusGreen else LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(50.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun ControlActionShelf(
    settings: CameraSettings,
    isRecording: Boolean,
    uiLocked: Boolean,
    lockHoldProgress: Float,
    onToggleAeLock: () -> Unit,
    onToggleGrid: () -> Unit,
    onToggleZebra: () -> Unit,
    onTogglePeaking: () -> Unit,
    onToggleLeveler: () -> Unit,
    onToggleProfile: () -> Unit,
    onShutterClick: () -> Unit,
    onLockClick: () -> Unit,
    onUnlockHoldStart: () -> Unit,
    onUnlockHoldCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(72.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Toggle Buttons Left Block (Only active if unlocked)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // AE/AF Lock
            HudToggleButton(
                icon = if (settings.aeAfLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = "Bloqueio AE/AF",
                active = settings.aeAfLocked,
                enabled = !uiLocked,
                onClick = onToggleAeLock
            )

            // Grid Layout Toggle
            HudToggleButton(
                icon = Icons.Default.Grid3x3,
                contentDescription = "Grade Cinematográfica",
                active = settings.gridVisible,
                enabled = !uiLocked,
                onClick = onToggleGrid
            )

            // Zebra Patterns Toggle
            HudToggleButton(
                icon = Icons.Default.Warning,
                contentDescription = "Zebra Highlight",
                active = settings.zebraPatternEnabled,
                enabled = !uiLocked,
                onClick = onToggleZebra
            )

            // Color Profile switch: REC709 vs LOG curves
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (settings.colorProfile == "LOG") CineRed else GraphiteGray)
                    .clickable(enabled = !uiLocked) { onToggleProfile() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = settings.colorProfile,
                    color = if (settings.colorProfile == "LOG") PureBlack else LightGray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Quick Peaking filter toggle
            HudToggleButton(
                icon = Icons.Default.FilterCenterFocus,
                contentDescription = "Focus Peaking",
                active = settings.focusPeakingEnabled,
                enabled = !uiLocked,
                onClick = onTogglePeaking
            )

            // Leveler sensor indicator toggle
            HudToggleButton(
                icon = Icons.Default.RotateLeft,
                contentDescription = "Indicador de Nível",
                active = settings.levelerEnabled,
                enabled = !uiLocked,
                onClick = onToggleLeveler
            )
        }

        // Center Shutter Button (Recording)
        Box(
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .size(68.dp)
                .border(2.5.dp, if (isRecording) CineRed else LightGray, CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .background(PureBlack)
                .clickable(
                    enabled = !uiLocked,
                    onClick = onShutterClick
                ),
            contentAlignment = Alignment.Center
        ) {
            val recordStateProgress by animateDpAsState(
                targetValue = if (isRecording) 24.dp else 52.dp,
                animationSpec = tween(250),
                label = "shutter_size"
            )

            val recordStateShape = if (isRecording) RoundedCornerShape(6.dp) else CircleShape

            Box(
                modifier = Modifier
                    .size(recordStateProgress)
                    .clip(recordStateShape)
                    .background(CineRed)
            )
        }

        // System lock icon side (Padlock HOLD-To-Unlock interaction)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (uiLocked) {
                // Displays holding unlock indicator ring
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onUnlockHoldStart()
                                    tryAwaitRelease()
                                    onUnlockHoldCancel()
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Continuous hold arc background canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = MediumGray.copy(alpha = 0.2f),
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawArc(
                            color = CineRed,
                            startAngle = -90f,
                            sweepAngle = lockHoldProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Desbloquear",
                        tint = CineRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // Interactive padlock to engage UI lock
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GraphiteGray)
                        .clickable { onLockClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Bloquear",
                        tint = LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HudToggleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    active: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (active) CineRed else GraphiteGray)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (active) PureBlack else LightGray,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
fun InteractiveConfigSelector(
    title: String,
    options: List<String>,
    selectedOption: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack.copy(alpha = 0.75f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepGray, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .border(0.5.dp, CineRed.copy(alpha = 0.3f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .clickable(enabled = false) {}
                .padding(24.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CineRed,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) CineRed else GraphiteGray)
                            .clickable { onSelect(option) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = option,
                            color = if (isSelected) PureBlack else LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GraphiteGray,
                    contentColor = LightGray
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "FECHAR",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
