package com.matancita.loteria.ui.theme.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
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
import java.util.Calendar
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- Modelos de Datos y Contenido ---

sealed class CircleContent {
    data class Number(val value: String) : CircleContent()
    data class Icon(val imageVector: ImageVector) : CircleContent()
}

data class HiddenCircle(
    val content: CircleContent,
    val center: Offset,
    val radius: Float
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

    val luckyNumbers = remember { getDailyLuckyNumbers(3) }
    val scope = rememberCoroutineScope() // Se obtiene el CoroutineScope aquí

    // --- AÑADIR ESTO ---
    val context = LocalContext.current
    val activity = LocalActivity.current
    var showInterstitialTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Asegúrate de usar tu ID de intersticial real aquí
        InterstitialAdManager.loadAd(context, "ca-app-pub-9861862421891852/3574997556")
    }
    if (showInterstitialTrigger) {
        LaunchedEffect(Unit) {
            activity?.let {
                InterstitialAdManager.showAd(it) {
                    // Acción opcional cuando el anuncio se cierra
                }
            }
            showInterstitialTrigger = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        StarryNightBackground()

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.find_fourtune),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                style = TextStyle(shadow = Shadow(Color.White.copy(alpha = 0.5f), blurRadius = 10f))
            )
            Text(
                text = stringResource(R.string.find_fourtune_desc),
                fontSize = 16.sp,
                fontFamily = FontFamily.Serif,
                color = Color.White.copy(alpha = 0.7f),
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
                    if (hiddenCircles.isEmpty()) {
                        hiddenCircles = generateRandomCircles(
                            luckyNumbers = luckyNumbers,
                            iconCount = 80,
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
                        scope = scope, // Se pasa el scope al canvas
                        onUpdateTouchPosition = { newPosition ->
                            touchPosition = newPosition
                            checkForFoundNumbers(newPosition, hiddenCircles, foundNumbers) { foundNumber ->
                                // --- CAMBIO CLAVE AQUÍ ---
                                // Verifica si este es el último número a encontrar
                                if (foundNumbers.size + 1 == luckyNumbers.size) {
                                    showInterstitialTrigger = true
                                }
                                foundNumbers = foundNumbers + foundNumber
                            }
                        }
                    )
                }
            }
        }
    }
}


// --- Canvas y Lógica de Dibujo ---

@Composable
private fun HiddenItemsCanvas(
    circles: List<HiddenCircle>,
    touchPosition: Offset?,
    foundNumbers: Set<String>,
    scope: CoroutineScope, // Se recibe el scope
    onUpdateTouchPosition: (Offset?) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val iconPainters = natureIcons.associateWith { rememberVectorPainter(image = it) }
    val magnifierRadius = 150f
    val magnifierVerticalOffset = 300f
    val foundColor = Color(0xFFFFD700)
    val haptic = LocalHapticFeedback.current
    var lastVibratedIndex by remember { mutableStateOf<Int?>(null) }


    val animatedAlphas = circles.map { circle ->
        val isUnderMagnifier = touchPosition?.let { (circle.center - it).getDistance() < magnifierRadius / 2f } ?: false
        val isPermanentlyFound = circle.content is CircleContent.Number && foundNumbers.contains(circle.content.value)
        val isVisible = isUnderMagnifier || isPermanentlyFound

        val targetContentAlpha = if (isVisible) 1.0f else if (circle.content is CircleContent.Number) 0.0f else 0.05f
        val contentAlpha by animateFloatAsState(targetValue = targetContentAlpha, animationSpec = tween(300), label = "contentAlpha")
        val circleAlpha by animateFloatAsState(targetValue = if (isVisible) 0.6f else 0.05f, animationSpec = tween(300), label = "circleAlpha")
        contentAlpha to circleAlpha
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { startOffset -> onUpdateTouchPosition(startOffset) },
                    onDrag = { change, _ ->
                        onUpdateTouchPosition(change.position)
                        // Se pasa el scope a la función de vibración
                        triggerHapticFeedback(change.position, circles, lastVibratedIndex, haptic, scope) { newIndex ->
                            lastVibratedIndex = newIndex
                        }
                        change.consume()
                    },
                    onDragEnd = {
                        onUpdateTouchPosition(null)
                        lastVibratedIndex = null
                    },
                    onDragCancel = {
                        onUpdateTouchPosition(null)
                        lastVibratedIndex = null
                    }
                )
            }
    ) {
        // --- 1. Dibuja la capa base de círculos ocultos ---
        circles.forEachIndexed { index, circle ->
            val (contentAlpha, circleAlpha) = animatedAlphas[index]
            val isPermanentlyFound = circle.content is CircleContent.Number && foundNumbers.contains(circle.content.value)
            val textColor = if (isPermanentlyFound) foundColor else Color.White

            drawCircle(color = Color.White, radius = circle.radius, center = circle.center, style = Stroke(width = 1.dp.toPx()), alpha = circleAlpha)
            drawCircleContent(
                circle = circle,
                alpha = contentAlpha,
                textMeasurer = textMeasurer,
                iconPainters = iconPainters,
                textColor = textColor,
                isIncandescent = isPermanentlyFound,
                foundColor = foundColor
            )
        }

        // --- 2. Dibuja el efecto de la lupa si el dedo está en la pantalla ---
        touchPosition?.let { touchPos ->
            val magnifierCenter = touchPos.copy(y = touchPos.y - magnifierVerticalOffset)
            val magnificationScale = 1.8f

            drawWithMagnification(
                touchPosition = touchPos,
                magnifierCenter = magnifierCenter,
                magnifierRadius = magnifierRadius,
                scale = magnificationScale
            ) {
                drawRect(color = Color(0xFF00001a))
                circles.forEach { circle ->
                    val isFound = circle.content is CircleContent.Number && foundNumbers.contains(circle.content.value)
                    val magnifiedTextColor = if (isFound) foundColor else Color.White
                    val filter = if (circle.content is CircleContent.Icon) ColorFilter.tint(Color.White) else null

                    drawCircle(color = Color.White, radius = circle.radius, center = circle.center, style = Stroke(width = 1.dp.toPx()))
                    drawCircleContent(
                        circle = circle,
                        alpha = 1f,
                        textMeasurer = textMeasurer,
                        iconPainters = iconPainters,
                        colorFilter = filter,
                        textColor = magnifiedTextColor,
                        isIncandescent = isFound,
                        foundColor = foundColor
                    )
                }
            }
        }
    }
}

private fun triggerHapticFeedback(
    touchPosition: Offset,
    circles: List<HiddenCircle>,
    lastVibratedIndex: Int?,
    haptic: HapticFeedback,
    scope: CoroutineScope, // Se recibe el scope para lanzar la corrutina de vibración
    onVibrated: (Int?) -> Unit
) {
    val detectionRadius = 40f
    val hoveredIndex = circles.indexOfFirst { (it.center - touchPosition).getDistance() < detectionRadius }

    if (hoveredIndex != -1) {
        if (hoveredIndex != lastVibratedIndex) {
            val content = circles[hoveredIndex].content
            if (content is CircleContent.Number) {
                scope.launch {
                    repeat(2) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        delay(60)
                    }
                }
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            onVibrated(hoveredIndex)
        }
    } else {
        onVibrated(null)
    }
}

private fun DrawScope.drawWithMagnification(
    touchPosition: Offset,
    magnifierCenter: Offset,
    magnifierRadius: Float,
    scale: Float,
    contentToMagnify: DrawScope.() -> Unit
) {
    val magnifierPath = createTeardropPath(magnifierCenter, magnifierRadius, touchPosition)

    // Dibuja el contenido magnificado
    clipPath(path = magnifierPath) {
        translate(left = magnifierCenter.x, top = magnifierCenter.y) {
            scale(scale = scale, pivot = Offset.Zero) {
                translate(left = -touchPosition.x, top = -touchPosition.y) {
                    contentToMagnify()
                }
            }
        }
    }

    // Dibuja el efecto de cristal sobre el contenido magnificado
    drawGlassyTeardrop(path = magnifierPath, center = magnifierCenter, radius = magnifierRadius)
}

// NUEVA FUNCIÓN para dibujar el efecto de cristal
private fun DrawScope.drawGlassyTeardrop(path: Path, center: Offset, radius: Float) {
    // 1. Dibuja un cuerpo de cristal translúcido
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.1f), Color(0xFF89CFF0).copy(alpha = 0.1f)),
            center = center,
            radius = radius
        )
    )

    // 2. Dibuja el borde exterior
    drawPath(
        path = path,
        style = Stroke(width = 3.dp.toPx()),
        color = Color(0xFF89CFF0).copy(alpha = 0.8f)
    )

    // 3. Dibuja un reflejo brillante en la parte superior para dar efecto 3D
    val highlightPath = Path().apply {
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(center = center, radius = radius),
            startAngleDegrees = -160f,
            sweepAngleDegrees = 100f,
            forceMoveTo = false
        )
    }
    drawPath(
        path = highlightPath,
        style = Stroke(width = 4.dp.toPx()),
        brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0.0f)))
    )
}


private fun createTeardropPath(topCenter: Offset, radius: Float, bottomTip: Offset): Path {
    return Path().apply {
        val stemWidth = 20f
        moveTo(bottomTip.x - stemWidth / 2, bottomTip.y)
        lineTo(topCenter.x - radius, topCenter.y)
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(center = topCenter, radius = radius),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        lineTo(bottomTip.x + stemWidth / 2, bottomTip.y)
        close()
    }
}


// --- Funciones de Ayuda (con cambios) ---

private fun DrawScope.drawCircleContent(
    circle: HiddenCircle,
    alpha: Float,
    textMeasurer: TextMeasurer,
    iconPainters: Map<ImageVector, androidx.compose.ui.graphics.painter.Painter>,
    colorFilter: ColorFilter? = null,
    textColor: Color = Color.White,
    isIncandescent: Boolean = false,
    foundColor: Color = Color(0xFFFFD700)
) {
    when (val content = circle.content) {
        is CircleContent.Number -> {
            val style = if (isIncandescent) {
                TextStyle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, foundColor),
                        center = center,
                        radius = circle.radius * 2
                    ),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(foundColor, blurRadius = 15f)
                )
            } else {
                TextStyle(color = textColor.copy(alpha = alpha), fontSize = 18.sp, fontWeight = FontWeight.Bold, shadow = Shadow(textColor.copy(alpha = alpha * 0.7f), blurRadius = 10f))
            }

            val textLayoutResult = textMeasurer.measure(
                text = content.value,
                style = style
            )
            drawText(textLayoutResult = textLayoutResult, topLeft = Offset(x = circle.center.x - textLayoutResult.size.width / 2, y = circle.center.y - textLayoutResult.size.height / 2))
        }
        is CircleContent.Icon -> {
            val painter = iconPainters[content.imageVector] ?: return
            translate(left = circle.center.x - circle.radius, top = circle.center.y - circle.radius) {
                with(painter) { draw(size = Size(circle.radius * 2, circle.radius * 2), alpha = alpha, colorFilter = colorFilter) }
            }
        }
    }
}

// --- Funciones de Ayuda (sin cambios) ---

private fun checkForFoundNumbers(
    touchPosition: Offset?, circles: List<HiddenCircle>, alreadyFound: Set<String>, onFound: (String) -> Unit
) {
    if (touchPosition == null) return
    val detectionRadius = 40f
    circles.forEach { circle ->
        if (circle.content is CircleContent.Number) {
            if ((circle.center - touchPosition).getDistance() < detectionRadius && !alreadyFound.contains(circle.content.value)) {
                onFound(circle.content.value)
            }
        }
    }
}

private fun getDailyLuckyNumbers(count: Int): List<String> {
    val calendar = Calendar.getInstance()
    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    val year = calendar.get(Calendar.YEAR)
    val seed = (dayOfYear * 31 + year * 365).toLong()
    val random = Random(seed)
    return List(count) { random.nextInt(1, 100).toString().padStart(2, '0') }
}

private fun generateRandomCircles(
    luckyNumbers: List<String>, iconCount: Int, width: Float, height: Float
): List<HiddenCircle> {
    val items = mutableListOf<HiddenCircle>()
    val contentList = luckyNumbers.map { CircleContent.Number(it) } + List(iconCount) { CircleContent.Icon(natureIcons.random()) }
    val shuffledContent = contentList.shuffled()
    val radius = 40f
    val minDistance = radius * 2.5f
    shuffledContent.forEach { content ->
        var validPosition = false
        var position = Offset.Zero
        var attempts = 0
        while (!validPosition && attempts < 100) {
            position = Offset(x = Random.nextFloat() * width, y = Random.nextFloat() * height)
            if (position.x < radius || position.x > width - radius || position.y < radius || position.y > height - radius) {
                continue // Asegura que el centro no esté muy cerca de los bordes
            }
            validPosition = items.none { (it.center - position).getDistance() < minDistance }
            attempts++
        }
        if (validPosition) {
            items.add(HiddenCircle(content, position, radius))
        }
    }
    return items
}

@Composable
private fun FoundNumbersIndicator(foundCount: Int, totalCount: Int) {
    val foundColor = Color(0xFFFFD700)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        for (i in 0 until totalCount) {
            val isFound = i < foundCount
            val color by animateColorAsState(targetValue = if (isFound) foundColor else Color.White.copy(alpha = 0.3f), animationSpec = tween(500), label = "indicatorColor")
            val sizeFactor by animateFloatAsState(targetValue = if (isFound) 1.2f else 1f, animationSpec = tween(300), label = "indicatorSize")
            Box(modifier = Modifier.size((14.dp * sizeFactor)).border(BorderStroke(1.dp, Color.White), CircleShape).background(color, CircleShape))
        }
    }
}
