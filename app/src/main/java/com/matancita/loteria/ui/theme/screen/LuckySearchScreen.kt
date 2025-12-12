package com.matancita.loteria.ui.theme.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matancita.loteria.R
import com.matancita.loteria.anuncios.InterstitialAdManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// --- Modelos ---
sealed class CircleContent {
    data class Number(val value: String) : CircleContent()
    data class Icon(val imageVector: ImageVector) : CircleContent()
}

data class HiddenCircle(
    val content: CircleContent,
    val center: Offset,
    val radius: Float
)

data class CelebrationParticle(
    val position: Offset,
    val angle: Double,
    val speed: Float,
    val color: Color,
    val createTime: Long
)

private val natureIcons = listOf(
    Icons.Default.LocalFlorist,
    Icons.Default.Forest,
    Icons.Default.Terrain,
    Icons.Default.WbSunny,
    Icons.Default.WaterDrop,
    Icons.Default.Park
)

// --- Pantalla Principal ---

@Composable
fun FindYourLuckScreen() {
    var hiddenCircles by remember { mutableStateOf<List<HiddenCircle>>(emptyList()) }
    var foundNumbers by remember { mutableStateOf<Set<String>>(emptySet()) }
    var touchPosition by remember { mutableStateOf<Offset?>(null) }

    // Estado de proximidad (0f = lejos, 1f = encima)
    var proximityToTarget by remember { mutableStateOf(0f) }
    var isUserInactive by remember { mutableStateOf(true) }

    // Partículas
    val particles = remember { mutableStateListOf<CelebrationParticle>() }

    val luckyNumbers = remember { getDailyLuckyNumbers(3) }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val activity = LocalActivity.current
    var showInterstitialTrigger by remember { mutableStateOf(false) }

    // Timer para detectar inactividad
    LaunchedEffect(touchPosition) {
        if (touchPosition != null) {
            isUserInactive = false
        } else {
            delay(3500)
            if (foundNumbers.size < luckyNumbers.size) {
                isUserInactive = true
            }
        }
    }

    // Loop de animación para partículas (Frame Loop)
    LaunchedEffect(Unit) {
        while (true) {
            val currentTime = System.currentTimeMillis()
            particles.removeAll { currentTime - it.createTime > 1000 }
            withFrameMillis { }
        }
    }

    LaunchedEffect(Unit) {
        InterstitialAdManager.loadAd(context, "ca-app-pub-9861862421891852/3574997556")
    }

    if (showInterstitialTrigger) {
        LaunchedEffect(Unit) {
            activity?.let {
                InterstitialAdManager.showAd(it) { }
            }
            showInterstitialTrigger = false
        }
    }

    // CORRECCIÓN 1: Fondo Oscuro aplicado al Box principal
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0D1117), // Azul noche muy oscuro
                        Color(0xFF161B22)  // Un tono ligeramente más claro
                    )
                )
            )
    ) {
        StarryNightBackground() // Ahora sí se verán las estrellas

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.find_fourtune),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif,
                color = Color.White,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0xFF89CFF0),
                        blurRadius = 25f
                    )
                )
            )

            Text(
                text = stringResource(R.string.find_fourtune_desc),
                fontSize = 15.sp,
                fontFamily = FontFamily.SansSerif,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            FoundNumbersIndicator(foundCount = foundNumbers.size, totalCount = luckyNumbers.size)

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current
                val widthPx = with(density) { maxWidth.toPx() }
                val heightPx = with(density) { maxHeight.toPx() }

                LaunchedEffect(widthPx, heightPx) {
                    if (hiddenCircles.isEmpty() && widthPx > 0 && heightPx > 0) {
                        hiddenCircles = generateRandomCircles(
                            luckyNumbers = luckyNumbers,
                            iconCount = 65,
                            width = widthPx,
                            height = heightPx
                        )
                    }
                }

                if (hiddenCircles.isNotEmpty()) {
                    HiddenItemsCanvas(
                        circles = hiddenCircles,
                        touchPosition = touchPosition,
                        foundNumbers = foundNumbers,
                        proximity = proximityToTarget,
                        particles = particles,
                        scope = scope,
                        onUpdateTouchPosition = { newPosition ->
                            touchPosition = newPosition

                            if (newPosition != null) {
                                val result = checkProximityAndFound(newPosition, hiddenCircles, foundNumbers)
                                proximityToTarget = result.first

                                result.second?.let { foundNum ->
                                    if (foundNumbers.size + 1 == luckyNumbers.size) {
                                        showInterstitialTrigger = true
                                    }
                                    foundNumbers = foundNumbers + foundNum

                                    repeat(20) {
                                        particles.add(
                                            CelebrationParticle(
                                                position = newPosition,
                                                angle = Random.nextDouble(0.0, 2 * Math.PI),
                                                speed = Random.nextFloat() * 10f + 2f,
                                                color = Color(0xFFFFD700),
                                                createTime = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                }
                            } else {
                                proximityToTarget = 0f
                            }
                        }
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = isUserInactive && touchPosition == null && foundNumbers.size < luckyNumbers.size,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    TutorialHint()
                }
            }
        }
    }
}

// --- Componente de Pista Visual ---
@Composable
fun TutorialHint() {
    val infiniteTransition = rememberInfiniteTransition(label = "hint")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "alpha"
    )
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 30f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOutQuad), RepeatMode.Reverse), label = "move"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset(y = offsetY.dp)
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.TouchApp,
            contentDescription = null,
            tint = Color.White.copy(alpha = alpha),
            modifier = Modifier.size(40.dp)
        )
        Text("Desliza para buscar", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
    }
}

// --- Canvas Principal ---

@Composable
private fun HiddenItemsCanvas(
    circles: List<HiddenCircle>,
    touchPosition: Offset?,
    foundNumbers: Set<String>,
    proximity: Float,
    particles: List<CelebrationParticle>,
    scope: CoroutineScope,
    onUpdateTouchPosition: (Offset?) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val iconPainters = natureIcons.associateWith { rememberVectorPainter(image = it) }

    val magnifierRadius = 130f
    val magnifierVerticalOffset = 220f
    val foundColor = Color(0xFFFFD700)
    val haptic = LocalHapticFeedback.current

    // CORRECCIÓN 2: Eliminamos la animación de la posición del toque (Animatable)
    // Usamos touchPosition directamente para eliminar el lag.

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val starPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pulse"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onUpdateTouchPosition(it) },
                    onDrag = { change, _ ->
                        onUpdateTouchPosition(change.position)

                        // Feedback háptico aleatorio si está caliente
                        if (proximity > 0.8f && Math.random() < 0.3) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        change.consume()
                    },
                    onDragEnd = { onUpdateTouchPosition(null) },
                    onDragCancel = { onUpdateTouchPosition(null) }
                )
            }
    ) {
        // Fondo Oscuro (Spotlight effect)
        if (touchPosition != null) {
            drawRect(color = Color.Black.copy(alpha = 0.5f)) // Un poco más oscuro para más contraste
        }

        // Elementos "Ocultos"
        circles.forEach { circle ->
            val isPermanentlyFound = circle.content is CircleContent.Number && foundNumbers.contains(circle.content.value)

            if (isPermanentlyFound) {
                drawCircleContent(
                    circle = circle,
                    alpha = 1f,
                    textMeasurer = textMeasurer,
                    iconPainters = iconPainters,
                    textColor = foundColor,
                    isIncandescent = true,
                    scale = starPulse
                )
            } else {
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = 1.5.dp.toPx(),
                    center = circle.center
                )
            }
        }

        // Efecto Lupa
        touchPosition?.let { pos ->
            val magCenter = pos.copy(y = pos.y - magnifierVerticalOffset)

            drawWithGlassyLens(
                touchPosition = pos,
                magnifierCenter = magCenter,
                magnifierRadius = magnifierRadius,
                proximity = proximity
            ) {
                drawRect(color = Color(0xFF000510))

                circles.forEach { circle ->
                    val isFound = circle.content is CircleContent.Number && foundNumbers.contains(circle.content.value)
                    val magnifiedTextColor = if (isFound) foundColor else Color.White

                    drawCircleContent(
                        circle = circle,
                        alpha = 1f,
                        textMeasurer = textMeasurer,
                        iconPainters = iconPainters,
                        colorFilter = if (circle.content is CircleContent.Icon) ColorFilter.tint(Color.White) else null,
                        textColor = magnifiedTextColor,
                        isIncandescent = isFound
                    )
                }
            }

            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha=0.0f), Color.White.copy(alpha=0.15f))
                ),
                start = magCenter.copy(y = magCenter.y + magnifierRadius * 0.8f),
                end = pos,
                strokeWidth = 2f
            )
        }

        // Partículas
        val currentTime = System.currentTimeMillis()
        particles.forEach { p ->
            val age = (currentTime - p.createTime) / 1000f
            if (age < 1f) {
                val moveDist = p.speed * age * 300f
                val x = p.position.x + cos(p.angle) * moveDist
                val y = p.position.y + sin(p.angle) * moveDist
                val alpha = (1f - age).coerceIn(0f, 1f)

                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = 3.dp.toPx() * (1f - age),
                    center = Offset(x.toFloat(), y.toFloat())
                )
            }
        }
    }
}

// --- Lógica del "Radar" (Proximidad) ---
private fun checkProximityAndFound(
    touchPos: Offset,
    circles: List<HiddenCircle>,
    foundNumbers: Set<String>
): Pair<Float, String?> {
    val detectionRadius = 40f
    val maxProximityRadius = 350f

    var closestDist = Float.MAX_VALUE
    var foundItem: String? = null

    circles.forEach { circle ->
        if (circle.content is CircleContent.Number) {
            val dist = (circle.center - touchPos).getDistance()

            if (dist < detectionRadius && !foundNumbers.contains(circle.content.value)) {
                foundItem = circle.content.value
            }

            if (!foundNumbers.contains(circle.content.value)) {
                if (dist < closestDist) {
                    closestDist = dist
                }
            }
        }
    }

    val proximity = (1f - (closestDist / maxProximityRadius)).coerceIn(0f, 1f)
    return Pair(proximity, foundItem)
}


// --- Dibujado de Lupa Reactiva ---
private fun DrawScope.drawWithGlassyLens(
    touchPosition: Offset,
    magnifierCenter: Offset,
    magnifierRadius: Float,
    proximity: Float,
    contentToMagnify: DrawScope.() -> Unit
) {
    val lensPath = Path().apply { addOval(Rect(center = magnifierCenter, radius = magnifierRadius)) }

    // Interpolación manual de color para evitar dependencias experimentales
    val coldColor = Color(0xFF89CFF0) // Azul
    val hotColor = Color(0xFFFF4500)  // Rojo

    // Mezcla de colores simple
    val activeColor = Color(
        red = coldColor.red + (hotColor.red - coldColor.red) * proximity,
        green = coldColor.green + (hotColor.green - coldColor.green) * proximity,
        blue = coldColor.blue + (hotColor.blue - coldColor.blue) * proximity,
        alpha = 1f
    )

    // 1. Contenido Magnificado
    clipPath(path = lensPath) {
        translate(left = magnifierCenter.x, top = magnifierCenter.y) {
            scale(scale = 2.0f, pivot = Offset.Zero) {
                translate(left = -touchPosition.x, top = -touchPosition.y) {
                    contentToMagnify()
                }
            }
        }
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                center = magnifierCenter,
                radius = magnifierRadius
            ),
            radius = magnifierRadius,
            center = magnifierCenter
        )
    }

    // 2. Borde Reactivo (Radar)
    val strokeWidth = 3.dp.toPx() + (proximity * 4.dp.toPx()) // Se hace más grueso al acercarse

    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(activeColor.copy(alpha=0.6f), Color.White, activeColor.copy(alpha=0.6f)),
            center = magnifierCenter
        ),
        radius = magnifierRadius,
        center = magnifierCenter,
        style = Stroke(width = strokeWidth)
    )

    // 3. Reflejo
    val highlightPath = Path().apply {
        addArc(Rect(center = magnifierCenter, radius = magnifierRadius - 10f), 200f, 60f)
    }
    drawPath(
        path = highlightPath,
        color = Color.White.copy(alpha = 0.5f),
        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
    )
}

// --- Dibujado de Items ---
private fun DrawScope.drawCircleContent(
    circle: HiddenCircle,
    alpha: Float,
    textMeasurer: TextMeasurer,
    iconPainters: Map<ImageVector, androidx.compose.ui.graphics.painter.Painter>,
    colorFilter: ColorFilter? = null,
    textColor: Color = Color.White,
    isIncandescent: Boolean = false,
    scale: Float = 1f
) {
    if (isIncandescent) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFD700).copy(alpha = 0.3f), Color.Transparent),
                center = circle.center,
                radius = circle.radius * 2.5f * scale
            ),
            center = circle.center,
            radius = circle.radius * 2.5f * scale
        )
    }

    when (val content = circle.content) {
        is CircleContent.Number -> {
            val textLayoutResult = textMeasurer.measure(
                text = content.value,
                style = TextStyle(
                    color = textColor.copy(alpha = alpha),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = if (isIncandescent) Shadow(Color.Black, blurRadius = 10f) else null
                )
            )
            val topLeft = Offset(
                circle.center.x - textLayoutResult.size.width / 2,
                circle.center.y - textLayoutResult.size.height / 2
            )

            withTransform({
                if(isIncandescent) scale(scale, circle.center)
            }) {
                drawText(textLayoutResult = textLayoutResult, topLeft = topLeft)
            }
        }
        is CircleContent.Icon -> {
            val painter = iconPainters[content.imageVector] ?: return
            translate(left = circle.center.x - circle.radius, top = circle.center.y - circle.radius) {
                with(painter) {
                    draw(
                        size = Size(circle.radius * 2, circle.radius * 2),
                        alpha = alpha * 0.6f,
                        colorFilter = colorFilter
                    )
                }
            }
        }
    }
}

// --- Indicador de Progreso ---
@Composable
private fun FoundNumbersIndicator(foundCount: Int, totalCount: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalCount) {
            val isFound = i < foundCount
            val color by animateColorAsState(if (isFound) Color(0xFFFFD700) else Color.White.copy(alpha=0.2f), label = "ind_col")
            val size by animateFloatAsState(if (isFound) 18f else 12f, label = "ind_size")

            Box(
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, Color.White.copy(alpha=0.5f), CircleShape)
            )
        }
    }
}

// --- Generadores ---
private fun getDailyLuckyNumbers(count: Int): List<String> {
    val calendar = Calendar.getInstance()
    val seed = (calendar.get(Calendar.DAY_OF_YEAR) * 31 + calendar.get(Calendar.YEAR) * 365).toLong()
    val random = java.util.Random(seed)
    return List(count) { random.nextInt(100).toString().padStart(2, '0') }
}

private fun generateRandomCircles(
    luckyNumbers: List<String>, iconCount: Int, width: Float, height: Float
): List<HiddenCircle> {
    val items = mutableListOf<HiddenCircle>()
    val allContent = (List(iconCount) { CircleContent.Icon(natureIcons.random()) } +
            luckyNumbers.map { CircleContent.Number(it) }).shuffled()

    val radius = 35f
    val minDistance = radius * 2.1f

    allContent.forEach { content ->
        var valid = false
        var pos = Offset.Zero
        var attempts = 0
        while (!valid && attempts < 100) {
            pos = Offset(
                x = Random.nextFloat() * (width - radius * 4) + radius * 2,
                y = Random.nextFloat() * (height - radius * 4) + radius * 2
            )
            valid = items.none { (it.center - pos).getDistance() < minDistance }
            attempts++
        }
        if (valid) items.add(HiddenCircle(content, pos, radius))
    }
    return items
}