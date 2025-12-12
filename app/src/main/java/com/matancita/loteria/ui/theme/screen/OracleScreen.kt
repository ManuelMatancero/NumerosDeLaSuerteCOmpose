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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
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

@Composable
fun OracleOfTimeScreen(viewModel: OracleViewModel = viewModel()) {
    val gameState by viewModel.gameState.collectAsState()
    val oracleNumbers by viewModel.oracleNumbers.collectAsState()

    // Fondo animado sutil "Respiración del Universo"
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val bgScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(10000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "bg_scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Capa 1: Fondo Sólido Profundo
        Box(modifier = Modifier.fillMaxSize().background(DeepVoid))

        // Capa 2: Nebulosa Animada
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.6f)) {
            withTransform({ scale(bgScale, center) }) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1A237E).copy(alpha = 0.5f), Color.Transparent),
                        radius = size.maxDimension * 0.8f,
                        center = center
                    )
                )
            }
        }

        // Capa 3: Estrellas
        StarryNightBackground()

        // Capa 4: Contenido UI Responsivo
        BoxWithConstraints(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            val isLandscape = maxWidth > maxHeight
            // Calculamos el tamaño ideal del reloj basado en la pantalla disponible
            val clockSize = if (isLandscape) maxHeight * 0.8f else maxWidth * 0.85f

            if (isLandscape) {
                LandscapeLayout(gameState, oracleNumbers ?: emptyList(), viewModel, clockSize)
            } else {
                PortraitLayout(gameState, oracleNumbers ?: emptyList(), viewModel, clockSize)
            }
        }
    }
}

@Composable
private fun PortraitLayout(
    gameState: OracleGameState,
    oracleNumbers: List<Int>,
    viewModel: OracleViewModel,
    clockSize: Dp
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OracleHeader()

        Spacer(modifier = Modifier.weight(1f))

        Box(contentAlignment = Alignment.Center) {
            CyberAstrolabe(
                modifier = Modifier.size(clockSize),
                gameState = gameState,
                numbers = oracleNumbers,
                onInteract = { viewModel.stopOracle() }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        OracleFooter(gameState, oracleNumbers)

        Spacer(modifier = Modifier.height(16.dp))
        AdmobAdaptiveBanner(adUnitId = "ca-app-pub-9861862421891852/2370788758")
    }
}

@Composable
private fun LandscapeLayout(
    gameState: OracleGameState,
    oracleNumbers: List<Int>,
    viewModel: OracleViewModel,
    clockSize: Dp
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OracleHeader()
            Spacer(modifier = Modifier.height(24.dp))
            OracleFooter(gameState, oracleNumbers)
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            CyberAstrolabe(
                modifier = Modifier.size(clockSize),
                gameState = gameState,
                numbers = oracleNumbers,
                onInteract = { viewModel.stopOracle() }
            )
        }
    }
}

// --- Componentes UI Refinados ---

@Composable
fun OracleHeader() {
    // Glassmorphism Container para legibilidad
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.oracle_title),
                fontSize = 28.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                style = TextStyle(shadow = Shadow(color = CyberBlue, blurRadius = 15f))
            )
            Text(
                text = "EL CRONOMETRISTA",
                fontSize = 12.sp,
                color = CyberBlue.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- LÓGICA DEL ASTROLABIO MEJORADA ---

data class Particle(
    val x: Float, val y: Float, val angle: Float, val speed: Float,
    val color: Color, val createdAt: Long, val lifeTime: Long
)

@Composable
fun CyberAstrolabe(
    modifier: Modifier = Modifier,
    gameState: OracleGameState,
    numbers: List<Int>,
    onInteract: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "main_loop")

    // Estados de animación
    val particles = remember { mutableStateListOf<Particle>() }
    val isRunning = gameState == OracleGameState.READY_TO_PLAY

    // Rotación base constante
    val baseRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing)), label = "rot"
    )

    // Física de Impacto (Spring Physics para sentirse pesado)
    val recoilAnim = remember { Animatable(1f) }

    // Detectar cambio de estado para disparar efectos
    LaunchedEffect(isRunning) {
        if (!isRunning) {
            // Efecto de retroceso "Golpe Mecánico"
            launch {
                recoilAnim.snapTo(0.92f)
                recoilAnim.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 400f))
            }
            // Haptic fuerte
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

            // Explosión de partículas
            repeat(40) {
                particles.add(
                    Particle(
                        x = 0f, y = 0f,
                        angle = Random.nextFloat() * 360f,
                        speed = Random.nextFloat() * 10f + 5f,
                        color = if(Random.nextBoolean()) NeonGold else CyberBlue,
                        createdAt = System.currentTimeMillis(),
                        lifeTime = Random.nextLong(600, 1200)
                    )
                )
            }
        }
    }

    // Loop de renderizado de partículas
    LaunchedEffect(Unit) {
        while(true) {
            val now = System.currentTimeMillis()
            particles.removeAll { now - it.createdAt > it.lifeTime }
            withFrameMillis { }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // 1. ANILLOS (Canvas Optimizado)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .scale(recoilAnim.value)
                .graphicsLayer { rotationZ = baseRotation }
        ) {
            val radius = size.minDimension / 2

            // Halo de Energía
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, CyberBlue.copy(0.15f), Color.Transparent),
                    radius = radius
                ),
                radius = radius
            )

            // Anillo Exterior (Ticks)
            drawCircle(
                color = CyberBlue.copy(0.4f),
                radius = radius,
                style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 25f)))
            )

            // Anillo Interior Giratorio (Contra-rotación para efecto visual)
            rotate(-baseRotation * 2) {
                drawCircle(
                    brush = Brush.sweepGradient(listOf(Color.Transparent, NeonGold.copy(0.8f), Color.Transparent)),
                    radius = radius * 0.88f,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // 2. RAYOS LÁSER (Manecillas)
        // Usamos Animatable para control total sobre la física de parada
        val hand1 = remember { Animatable(0f) }
        val hand2 = remember { Animatable(0f) }
        val hand3 = remember { Animatable(0f) }

        LaunchedEffect(isRunning) {
            if (isRunning) {
                // Modo Giro Libre
                launch { while(isActive) { hand1.snapTo(hand1.value + 8f); delay(16) } }
                launch { while(isActive) { hand2.snapTo(hand2.value + 12f); delay(16) } }
                launch { while(isActive) { hand3.snapTo(hand3.value - 10f); delay(16) } }
            } else {
                // Modo Aterrizaje Preciso (con rebote)
                val t1 = (numbers.getOrNull(0) ?: 0) * 3.6f + 720f // +720 para dar 2 vueltas antes de parar
                val t2 = (numbers.getOrNull(1) ?: 0) * 3.6f + 720f
                val t3 = (numbers.getOrNull(2) ?: 0) * 3.6f - 720f

                launch { hand1.animateTo(t1, spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow)) }
                launch { hand2.animateTo(t2, spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium)) }
                launch { hand3.animateTo(t3, spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow)) }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize(0.75f)) {
            val center = this.center
            val r = size.minDimension / 2

            fun drawLaser(angle: Float, color: Color, width: Float) {
                rotate(angle) {
                    // Haz de luz
                    drawLine(
                        brush = Brush.verticalGradient(listOf(Color.White.copy(0.9f), color, Color.Transparent)),
                        start = center,
                        end = Offset(center.x, center.y - r),
                        strokeWidth = width,
                        cap = StrokeCap.Round
                    )
                    // Punta brillante si está parado
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

        // 3. CAPA DE PARTÍCULAS
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

        // 4. EL NÚCLEO (Botón Interactivo)
        CoreButton(isRunning = isRunning, onClick = onInteract)
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
            .size(90.dp)
            .scale(currentScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isRunning,
                onClick = onClick
            )
    ) {
        // Halo externo
        if (isRunning) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(colors = listOf(mainColor.copy(0.4f), Color.Transparent))
                )
            }
        }

        // Botón Físico
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
            // Icono animado
            Crossfade(targetState = isRunning, label = "icon") { running ->
                Icon(
                    imageVector = if (running) Icons.Rounded.HourglassEmpty else Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = DeepVoid,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun OracleFooter(gameState: OracleGameState, numbers: List<Int>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(
            visible = gameState == OracleGameState.REVEALED,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Contenedor Glassmorphic para resultados
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF101020).copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DESTINO REVELADO",
                            color = NeonGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            numbers.forEachIndexed { index, num ->
                                var show by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) { delay(index * 150L); show = true }
                                AnimatedVisibility(visible = show, enter = scaleIn(spring(stiffness = Spring.StiffnessLow))) {
                                    ResultOrb(num)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.oracle_come_back_tomorrow),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        AnimatedVisibility(
            visible = gameState == OracleGameState.READY_TO_PLAY,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = "TOCA EL NÚCLEO PARA DETENER EL TIEMPO",
                color = CyberBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color.Black.copy(0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun ResultOrb(number: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val dy by infiniteTransition.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "dy"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(60.dp)
            .graphicsLayer { translationY = dy }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(colors = listOf(NeonGold.copy(0.4f), Color.Transparent))
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    Brush.radialGradient(listOf(Color.White.copy(0.2f), Color.Transparent)),
                    CircleShape
                )
                .border(2.dp, NeonGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString().padStart(2, '0'),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Serif
            )
        }
    }
}