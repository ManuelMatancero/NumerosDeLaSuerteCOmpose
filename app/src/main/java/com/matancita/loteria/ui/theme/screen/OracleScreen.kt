package com.matancita.loteria.ui.theme.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matancita.loteria.R
import com.matancita.loteria.anuncios.AdmobAdaptiveBanner
import com.matancita.loteria.viewmodel.OracleGameState
import com.matancita.loteria.viewmodel.OracleViewModel
import com.matancita.loteria.viewmodel.SyncRating
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// --- Paleta de Colores Cyber-Mística ---
private val NeonGold = Color(0xFFFFD700)
private val CyberBlue = Color(0xFF00E5FF)
private val DeepVoid = Color(0xFF050510)
private val PlasmaPurple = Color(0xFFD500F9)
private val SyncGreen = Color(0xFF00E676)
private val SyncOrange = Color(0xFFFF9100)
private val SyncRed = Color(0xFFFF5252)

@Composable
fun OracleOfTimeScreen(viewModel: OracleViewModel = viewModel()) {
    val gameState by viewModel.gameState.collectAsState()
    val oracleNumbers by viewModel.oracleNumbers.collectAsState()
    val syncResult by viewModel.syncResult.collectAsState()
    val targetAngle by viewModel.targetAngle.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val bgScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(10000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "bg_scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(DeepVoid))

        Canvas(modifier = Modifier.fillMaxSize().alpha(0.6f)) {
            withTransform({ scale(bgScale, bgScale, center) }) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1A237E).copy(alpha = 0.5f), Color.Transparent),
                        radius = size.maxDimension * 0.8f,
                        center = center
                    )
                )
            }
        }

        StarryNightBackground()

        BoxWithConstraints(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            val isLandscape = maxWidth > maxHeight
            // Constrain astrolabe so footer never gets clipped
            val clockSize = if (isLandscape) {
                minOf(maxHeight * 0.7f, maxWidth * 0.45f)
            } else {
                minOf(maxWidth * 0.72f, maxHeight * 0.42f)
            }

            if (isLandscape) {
                LandscapeLayout(gameState, oracleNumbers ?: emptyList(), syncResult, targetAngle, viewModel, clockSize)
            } else {
                PortraitLayout(gameState, oracleNumbers ?: emptyList(), syncResult, targetAngle, viewModel, clockSize)
            }
        }
    }
}

@Composable
private fun PortraitLayout(
    gameState: OracleGameState,
    oracleNumbers: List<Int>,
    syncResult: com.matancita.loteria.viewmodel.SyncResult?,
    targetAngle: Float,
    viewModel: OracleViewModel,
    clockSize: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OracleHeader()

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(clockSize)
        ) {
            CyberAstrolabe(
                modifier = Modifier.fillMaxSize(),
                gameState = gameState,
                numbers = oracleNumbers,
                targetAngle = targetAngle,
                onInteract = { stoppedAngle -> viewModel.stopOracle(stoppedAngle) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OracleFooter(gameState, oracleNumbers, syncResult)

        Spacer(modifier = Modifier.height(16.dp))
        AdmobAdaptiveBanner(adUnitId = "ca-app-pub-9861862421891852/2370788758")
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun LandscapeLayout(
    gameState: OracleGameState,
    oracleNumbers: List<Int>,
    syncResult: com.matancita.loteria.viewmodel.SyncResult?,
    targetAngle: Float,
    viewModel: OracleViewModel,
    clockSize: Dp
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: scrollable header + results
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OracleHeader()
            Spacer(modifier = Modifier.height(16.dp))
            OracleFooter(gameState, oracleNumbers, syncResult)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Right side: astrolabe, vertically centered
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            CyberAstrolabe(
                modifier = Modifier.size(clockSize),
                gameState = gameState,
                numbers = oracleNumbers,
                targetAngle = targetAngle,
                onInteract = { stoppedAngle -> viewModel.stopOracle(stoppedAngle) }
            )
        }
    }
}

@Composable
fun OracleHeader() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.oracle_title),
                fontSize = 24.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                style = TextStyle(shadow = Shadow(color = CyberBlue, blurRadius = 12f))
            )
            Text(
                text = stringResource(id = R.string.oracle_header_subtitle),
                fontSize = 11.sp,
                color = CyberBlue.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

data class Particle(
    val x: Float, val y: Float, val angle: Float, val speed: Float,
    val color: Color, val createdAt: Long, val lifeTime: Long
)

@Composable
fun CyberAstrolabe(
    modifier: Modifier = Modifier,
    gameState: OracleGameState,
    numbers: List<Int>,
    targetAngle: Float,
    onInteract: (Float) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "main_loop")

    val particles = remember { mutableStateListOf<Particle>() }
    val isRunning = gameState == OracleGameState.READY_TO_PLAY

    val baseRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing)), label = "rot"
    )

    val recoilAnim = remember { Animatable(1f) }
    val shockwaveRadius = remember { Animatable(0f) }
    val shockwaveAlpha = remember { Animatable(0.8f) }

    LaunchedEffect(isRunning) {
        if (!isRunning) {
            launch {
                recoilAnim.snapTo(0.92f)
                recoilAnim.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 400f))
            }
            launch {
                shockwaveRadius.snapTo(0f)
                shockwaveAlpha.snapTo(0.8f)
                shockwaveRadius.animateTo(1f, tween(800, easing = EaseOutCubic))
                shockwaveAlpha.animateTo(0f, tween(800, easing = EaseOutCubic))
            }
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

            repeat(50) {
                particles.add(
                    Particle(
                        x = 0f, y = 0f,
                        angle = Random.nextFloat() * 360f,
                        speed = Random.nextFloat() * 12f + 4f,
                        color = listOf(NeonGold, CyberBlue, PlasmaPurple, Color.White).random(),
                        createdAt = System.currentTimeMillis(),
                        lifeTime = Random.nextLong(500, 1500)
                    )
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        while(true) {
            val now = System.currentTimeMillis()
            particles.removeAll { now - it.createdAt > it.lifeTime }
            withFrameMillis { }
        }
    }

    val hand1 = remember { Animatable(0f) }
    val hand2 = remember { Animatable(0f) }
    val hand3 = remember { Animatable(0f) }
    var stoppedAngle by remember { mutableStateOf(0f) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            launch { while(isActive) { hand1.snapTo(hand1.value + 8f); delay(16) } }
            launch { while(isActive) { hand2.snapTo(hand2.value + 12f); delay(16) } }
            launch { while(isActive) { hand3.snapTo(hand3.value - 10f); delay(16) } }
        } else {
            val t1 = (numbers.getOrNull(0) ?: 0) * 3.6f + 720f
            val t2 = (numbers.getOrNull(1) ?: 0) * 3.6f + 720f
            val t3 = (numbers.getOrNull(2) ?: 0) * 3.6f - 720f

            launch { hand1.animateTo(t1, spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow)) }
            launch { hand2.animateTo(t2, spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium)) }
            launch { hand3.animateTo(t3, spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow)) }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // 1. RINGS
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .scale(recoilAnim.value)
                .graphicsLayer { rotationZ = baseRotation }
        ) {
            val radius = size.minDimension / 2

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, CyberBlue.copy(0.15f), Color.Transparent),
                    radius = radius
                ),
                radius = radius
            )

            drawCircle(
                color = CyberBlue.copy(0.4f),
                radius = radius,
                style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 25f)))
            )

            rotate(-baseRotation * 2) {
                drawCircle(
                    brush = Brush.sweepGradient(listOf(Color.Transparent, NeonGold.copy(0.8f), Color.Transparent)),
                    radius = radius * 0.88f,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // 2. SWEET SPOT TARGET (only when running)
        if (isRunning) {
            val targetPulse by infiniteTransition.animateFloat(
                initialValue = 0.85f, targetValue = 1.15f,
                animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "target_pulse"
            )
            Canvas(modifier = Modifier.fillMaxSize(0.82f)) {
                val center = this.center
                val r = size.minDimension / 2
                rotate(targetAngle + baseRotation) {
                    val x = center.x
                    val y = center.y - r
                    drawCircle(
                        color = SyncGreen.copy(alpha = 0.6f),
                        radius = 8.dp.toPx() * targetPulse,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        radius = 4.dp.toPx() * targetPulse,
                        center = Offset(x, y)
                    )
                }
            }
        }

        // 3. LASER HANDS
        Canvas(modifier = Modifier.fillMaxSize(0.75f)) {
            val center = this.center
            val r = size.minDimension / 2

            fun drawLaser(angle: Float, color: Color, width: Float) {
                rotate(angle) {
                    drawLine(
                        brush = Brush.verticalGradient(listOf(Color.White.copy(0.9f), color, Color.Transparent)),
                        start = center,
                        end = Offset(center.x, center.y - r),
                        strokeWidth = width,
                        cap = StrokeCap.Round
                    )
                    if (!isRunning) {
                        drawCircle(color = Color.White, radius = width, center = Offset(center.x, center.y - r))
                        drawCircle(color = color.copy(0.5f), radius = width * 3, center = Offset(center.x, center.y - r))
                    }
                }
            }

            drawLaser(hand1.value, CyberBlue, 4.dp.toPx())
            drawLaser(hand2.value, NeonGold, 5.dp.toPx())
            drawLaser(hand3.value, PlasmaPurple, 3.dp.toPx())
        }

        // 4. SHOCKWAVE
        if (!isRunning && shockwaveRadius.value > 0.01f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2 * shockwaveRadius.value
                drawCircle(
                    color = NeonGold.copy(alpha = shockwaveAlpha.value),
                    radius = radius,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        // 5. PARTICLES
        Canvas(modifier = Modifier.fillMaxSize()) {
            val now = System.currentTimeMillis()
            particles.forEach { p ->
                val elapsed = (now - p.createdAt).toFloat()
                val progress = elapsed / p.lifeTime
                if (progress < 1f) {
                    val dist = p.speed * elapsed / 12f
                    val rad = Math.toRadians(p.angle.toDouble())
                    val x = center.x + cos(rad).toFloat() * dist
                    val y = center.y + sin(rad).toFloat() * dist
                    val alpha = 1f - progress
                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = 3.dp.toPx() * (1f - progress),
                        center = Offset(x, y)
                    )
                }
            }
        }

        // 6. CORE BUTTON
        CoreButton(
            isRunning = isRunning,
            onClick = {
                stoppedAngle = (hand1.value % 360 + 360) % 360
                onInteract(stoppedAngle)
            }
        )
    }
}

@Composable
fun CoreButton(isRunning: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "core")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse), label = "pulse"
    )

    val currentScale = if (isPressed) 0.9f else if (isRunning) pulse else 1f
    val mainColor = if (isRunning) CyberBlue else NeonGold

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(100.dp)
            .scale(currentScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isRunning,
                onClick = onClick
            )
    ) {
        if (isRunning) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(colors = listOf(mainColor.copy(0.4f), Color.Transparent))
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize(0.75f)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(0.9f), mainColor, mainColor.copy(0.6f))
                    ),
                    shape = CircleShape
                )
                .border(2.dp, Color.White, CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = isRunning, label = "icon") { running ->
                Icon(
                    imageVector = if (running) Icons.Rounded.HourglassEmpty else Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = DeepVoid,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun OracleFooter(
    gameState: OracleGameState,
    numbers: List<Int>,
    syncResult: com.matancita.loteria.viewmodel.SyncResult?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(
            visible = gameState == OracleGameState.REVEALED,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Sync Result Badge
                syncResult?.let { sync ->
                    val (syncText, syncColor) = when (sync.rating) {
                        SyncRating.PERFECT -> stringResource(R.string.oracle_sync_perfect) to SyncGreen
                        SyncRating.GREAT -> stringResource(R.string.oracle_sync_great) to CyberBlue
                        SyncRating.GOOD -> stringResource(R.string.oracle_sync_good) to SyncOrange
                        SyncRating.MISSED -> stringResource(R.string.oracle_sync_missed) to SyncRed
                    }
                    Surface(
                        color = syncColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, syncColor.copy(alpha = 0.5f)),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = syncText.uppercase(),
                                color = syncColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${sync.accuracyPercent}%",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Accuracy Bar
                    LinearProgressIndicator(
                        progress = { sync.accuracyPercent / 100f },
                        color = syncColor,
                        trackColor = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(4.dp)
                            .padding(bottom = 16.dp)
                    )
                }

                // Results Container
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF101020).copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(id = R.string.oracle_destiny_revealed),
                            color = NeonGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            numbers.forEachIndexed { index, num ->
                                var show by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) { delay(index * 180L); show = true }
                                AnimatedVisibility(visible = show, enter = scaleIn(spring(stiffness = Spring.StiffnessLow))) {
                                    ResultOrb(num, isHighlighted = index == 0)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.oracle_come_back_tomorrow),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        AnimatedVisibility(
            visible = gameState == OracleGameState.REVEALING,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = NeonGold,
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(id = R.string.oracle_chrono_resonance),
                    color = NeonGold.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        AnimatedVisibility(
            visible = gameState == OracleGameState.READY_TO_PLAY,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = stringResource(id = R.string.oracle_tap_core_instruction),
                color = CyberBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color.Black.copy(0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
fun ResultOrb(number: Int, isHighlighted: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val dy by infiniteTransition.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "dy"
    )

    val orbSize = if (isHighlighted) 62.dp else 54.dp
    val textSize = if (isHighlighted) 22.sp else 20.sp
    val glowColor = if (isHighlighted) NeonGold else CyberBlue

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(orbSize)
            .graphicsLayer { translationY = dy }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(colors = listOf(glowColor.copy(0.5f), Color.Transparent))
            )
        }
        Box(
            modifier = Modifier
                .size(if (isHighlighted) 48.dp else 42.dp)
                .background(
                    Brush.radialGradient(listOf(Color.White.copy(0.2f), Color.Transparent)),
                    CircleShape
                )
                .border(if (isHighlighted) 2.5.dp else 1.5.dp, glowColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString().padStart(2, '0'),
                fontSize = textSize,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Serif
            )
        }
    }
}
