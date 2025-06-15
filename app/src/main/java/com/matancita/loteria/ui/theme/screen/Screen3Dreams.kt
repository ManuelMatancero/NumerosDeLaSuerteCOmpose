package com.matancita.loteria.ui.theme.screen

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Bed
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matancita.loteria.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// --- Colores (puedes personalizarlos en tu Theme.kt) ---
val NightSkyStart = Color(0xFF0D1117)
val NightSkyEnd = Color(0xFF161B22)
val LuckyGreen = Color(0xFF2E7D32)
val GoldAccent = Color(0xFFFFC107)

fun Color.darken(factor: Float): Color {
    val invertedFactor = (1 - factor).coerceIn(0f, 1f)
    return Color(red = this.red * invertedFactor, green = this.green * invertedFactor, blue = this.blue * invertedFactor, alpha = this.alpha)
}

// --- Modelos de datos ---
data class DreamItem(val number: Int, val text: String, val meaning: String)
private data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val baseAlpha: Float
)

// --- Funciones de Composición Principales ---

@Composable
fun rememberDreamList(): List<DreamItem> {
    val dreamNames = stringArrayResource(id = R.array.dream_names)
    val dreamMeanings = stringArrayResource(id = R.array.dream_meanings)
    return remember(dreamNames, dreamMeanings) {
        dreamNames.mapIndexed { index, name ->
            DreamItem(
                number = index + 1,
                text = name,
                meaning = dreamMeanings.getOrElse(index) { "" }
            )
        }
    }
}

@Composable
fun Screen3Dreams() {
    val allDreamExperiences = rememberDreamList()
    var searchQuery by remember { mutableStateOf("") }
    var selectedDream by remember { mutableStateOf<DreamItem?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

    val filteredDreamItems by remember(searchQuery, allDreamExperiences) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                allDreamExperiences
            } else {
                val queryLower = searchQuery.lowercase()
                allDreamExperiences.filter { dreamItem ->
                    dreamItem.text.lowercase().contains(queryLower) || dreamItem.number.toString().contains(queryLower)
                }
            }
        }
    }

    val onDreamItemClickLambda = { dream: DreamItem ->
        if (selectedDream?.number != dream.number) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        selectedDream = dream
        keyboardController?.hide()
    }

    // --- LÓGICA DE AGITADO ACTUALIZADA ---
    ShakeToReveal(
        threshold = 400f,
        onShake = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            // 1. Limpiamos cualquier búsqueda para asegurar que el sueño esté en la lista visible.
            searchQuery = ""
            // 2. Seleccionamos un nuevo sueño al azar. El LaunchedEffect de abajo se encargará del scroll.
            selectedDream = allDreamExperiences.random()
        }
    )

    // --- NUEVO: LaunchedEffect para hacer scroll automático ---
    // Este efecto se ejecuta cada vez que 'selectedDream' cambia.
    LaunchedEffect(selectedDream) {
        // Nos aseguramos de que haya un sueño seleccionado
        selectedDream?.let { dream ->
            // Buscamos el índice del sueño en la lista que se está mostrando actualmente
            val index = filteredDreamItems.indexOf(dream)
            // Si el sueño se encuentra en la lista (el índice es válido)
            if (index != -1) {
                // Hacemos que la lista se desplace suavemente hasta ese elemento
                lazyListState.animateScrollToItem(index)
            }
        }
    }

    // --- El resto de la UI no cambia ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NightSkyStart, NightSkyEnd)))
    ) {
        StarryNightBackground()

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Screen3DreamsPortrait(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedDream = selectedDream,
                onDreamItemClick = { onDreamItemClickLambda(it) },
                filteredDreamItems = filteredDreamItems,
                keyboardController = keyboardController,
                lazyListState = lazyListState
            )
        } else {
            Screen3DreamsLandscape(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedDream = selectedDream,
                onDreamItemClick = { onDreamItemClickLambda(it) },
                filteredDreamItems = filteredDreamItems,
                keyboardController = keyboardController,
                lazyListState = lazyListState
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Screen3DreamsPortrait(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedDream: DreamItem?,
    onDreamItemClick: (DreamItem) -> Unit,
    filteredDreamItems: List<DreamItem>,
    keyboardController: SoftwareKeyboardController?,
    lazyListState: LazyListState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.dreams_oracle_title),
            style = MaterialTheme.typography.headlineLarge,
            fontFamily = FontFamily.Serif,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        InteractiveNumberOrb(selectedNumber = selectedDream?.number)

        Text(
            text = stringResource(id = R.string.dreams_shake_hint),
            style = MaterialTheme.typography.bodySmall,
            color = GoldAccent.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
        )

        DreamSearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            keyboardController = keyboardController,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        DreamListContent(
            modifier = Modifier.weight(1f),
            filteredDreamItems = filteredDreamItems,
            onDreamItemClick = onDreamItemClick,
            selectedDreamNumber = selectedDream?.number,
            lazyListState = lazyListState
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Screen3DreamsLandscape(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedDream: DreamItem?,
    onDreamItemClick: (DreamItem) -> Unit,
    filteredDreamItems: List<DreamItem>,
    keyboardController: SoftwareKeyboardController?,
    lazyListState: LazyListState
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .systemBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.dreams_oracle_title),
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            InteractiveNumberOrb(selectedNumber = selectedDream?.number, orbSize = 130.dp)
            Text(
                text = stringResource(id = R.string.dreams_shake_hint),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = GoldAccent.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DreamSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                keyboardController = keyboardController,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            DreamListContent(
                modifier = Modifier.weight(1f),
                filteredDreamItems = filteredDreamItems,
                onDreamItemClick = onDreamItemClick,
                selectedDreamNumber = selectedDream?.number,
                lazyListState = lazyListState
            )
        }
    }
}

// --- Componentes Reutilizables y Mejorados ---

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DreamSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    keyboardController: SoftwareKeyboardController?,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(id = R.string.dreams_search_placeholder_long), color = LuckyGreen.copy(alpha = 0.7f)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon", tint = LuckyGreen) },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear Search", tint = LuckyGreen)
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = LuckyGreen.darken(0.2f),
            unfocusedTextColor = LuckyGreen.darken(0.1f),
            focusedContainerColor = Color.White.copy(alpha = 0.15f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
            cursorColor = GoldAccent,
            focusedBorderColor = GoldAccent,
            unfocusedBorderColor = LuckyGreen.copy(alpha = 0.4f),
        ),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DreamListContent(
    modifier: Modifier = Modifier,
    filteredDreamItems: List<DreamItem>,
    onDreamItemClick: (DreamItem) -> Unit,
    lazyListState: LazyListState,
    selectedDreamNumber: Int?
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        if (filteredDreamItems.isEmpty()) {
            Text(
                text = stringResource(id = R.string.dreams_no_results),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 48.dp)
            )
        } else {
            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredDreamItems, key = { it.number }) { dreamItem ->
                    DreamListItem(
                        dreamItem = dreamItem,
                        isSelected = dreamItem.number == selectedDreamNumber,
                        onItemClick = { onDreamItemClick(dreamItem) },
                        modifier = Modifier.animateItemPlacement(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveNumberOrb(
    selectedNumber: Int?,
    orbSize: Dp = 150.dp
) {
    val density = LocalDensity.current

    // --- NUEVO: Animación de flotación contínua ---
    val infiniteTransition = rememberInfiniteTransition(label = "orb-float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "orb-offset"
    )

    var animationState by remember(selectedNumber) { mutableStateOf(if (selectedNumber == null) "IDLE" else "REVEALED") }
    val animatableScale = remember { Animatable(1.0f) }
    val numberAlpha by animateFloatAsState(
        targetValue = if (animationState == "REVEALED") 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 200), label = ""
    )
    val particleAlpha by animateFloatAsState(
        targetValue = if (animationState == "REVEALING") 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = CubicBezierEasing(0f, 1f, 0.5f, 1f)), label = ""
    )

    LaunchedEffect(selectedNumber) {
        if (selectedNumber != null) {
            animationState = "REVEALING"
            delay(400) // Tiempo para el efecto de partículas
            animationState = "REVEALED"
            // Secuencia de escala para un efecto "pop"
            animatableScale.snapTo(0.8f)
            animatableScale.animateTo(1.1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
            animatableScale.animateTo(1.0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
        } else {
            animatableScale.animateTo(1.0f)
        }
    }

    val numberFontSize: TextUnit = (orbSize.value * 0.45f).sp
    val placeholderFontSize: TextUnit = (orbSize.value * 0.5f).sp

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(orbSize)
            .graphicsLayer {
                translationY = floatOffset
                shadowElevation = 24.dp.toPx()
                shape = CircleShape
                clip = true
                scaleX = animatableScale.value
                scaleY = animatableScale.value
            }
    ) {
        // Fondo del orbe
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(GoldAccent.copy(alpha = 0.5f), GoldAccent.darken(0.6f)),
                        radius = with(density) { orbSize.toPx() }
                    ),
                    shape = CircleShape
                )
                .border(2.dp, GoldAccent.copy(alpha = 0.8f), CircleShape)
        )

        // Partículas de revelación
        Canvas(modifier = Modifier
            .matchParentSize()
            .graphicsLayer { alpha = particleAlpha }) {
            val center = this.center
            val radius = size.minDimension / 2
            repeat(60) {
                val angle = Random.nextFloat() * 2 * Math.PI
                val speed = Random.nextFloat() * 1.5f + 0.5f
                val startRadius = radius * (1 - particleAlpha) * speed
                val xPos = center.x + cos(angle).toFloat() * startRadius
                val yPos = center.y + sin(angle).toFloat() * startRadius
                drawCircle(
                    color = Color.White,
                    radius = (Random.nextFloat() * 1.5f + 1f) * (1 - particleAlpha),
                    center = Offset(xPos, yPos),
                    alpha = (sin(Random.nextFloat() * Math.PI)).toFloat()
                )
            }
        }

        // Número o Placeholder
        val textToShow = selectedNumber?.toString()?.padStart(2, '0') ?: "?"
        Text(
            text = textToShow,
            fontSize = if (selectedNumber != null) numberFontSize else placeholderFontSize,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif,
            color = NightSkyStart.copy(alpha = 0.8f),
            modifier = Modifier.graphicsLayer { alpha = if (selectedNumber != null) numberAlpha else 1f }
        )

        // Reflejo de luz superior
        if (selectedNumber != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = numberAlpha }
                    .drawWithCache {
                        val brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
                            center = Offset(size.width * 0.5f, size.height * 0.2f),
                            radius = size.width * 0.4f
                        )
                        onDrawWithContent {
                            drawContent()
                            drawCircle(brush)
                        }
                    }
            )
        }
    }
}

// --- DreamListItem ACTUALIZADO ---

@Composable
fun DreamListItem(
    dreamItem: DreamItem,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(if (isSelected) 8.dp else 2.dp, label = "elevation")
    val borderColor by animateColorAsState(if (isSelected) GoldAccent else LuckyGreen.copy(alpha = 0.3f), label = "border-color")
    val containerColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface,
        label = "container-color"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = GoldAccent),
                onClick = onItemClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = borderColor),
        shadowElevation = elevation
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp) // Ajuste ligero del padding
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp) // Ajuste ligero del espaciado
        ) {
            // --- AQUÍ ESTÁ EL CAMBIO ---
            // Hemos reemplazado el Text del número por nuestro nuevo ícono animado.
            AnimatedBedIcon()
            // --- FIN DEL CAMBIO ---

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dreamItem.text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dreamItem.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isSelected) 10 else 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(id = R.string.dreams_details_cd),
                tint = GoldAccent.copy(alpha = if (isSelected) 0.9f else 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// --- NUEVO: Fondo de Estrellas ---
/**
 * Representa el estado de una estrella fugaz.
 */
private data class ShootingStarState(
    val startX: Float,
    val startY: Float,
    val speed: Float,
    val angle: Float,
    val tailLength: Float,
)

@Composable
fun StarryNightBackground(starCount: Int = 200) {
    // 1. Se recuerda la lista de estrellas para que no se regenere en cada recomposición.
    val stars = remember {
        List(starCount) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 2.2f + 0.8f,
                baseAlpha = Random.nextFloat() * 0.5f + 0.5f // Alfa base entre 0.5 y 1.0
            )
        }
    }

    // 2. Se crean `Animatable` para controlar el alfa de cada estrella individualmente.
    val animatables = remember { stars.map { Animatable(it.baseAlpha) } }

    // 3. Se usa LaunchedEffect para la animación de parpadeo de las estrellas.
    LaunchedEffect(Unit) {
        animatables.forEachIndexed { index, animatable ->
            if (index % 3 == 0) { // Solo un subconjunto parpadea
                launch {
                    delay(Random.nextLong(0, 1000))
                    animatable.animateTo(
                        targetValue = 0.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = Random.nextInt(1500, 5000),
                                easing = LinearEasing
                            ),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                }
            }
        }
    }

    // --- Lógica de la Estrella Fugaz ---
    var shootingStar by remember { mutableStateOf<ShootingStarState?>(null) }
    val shootingStarProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(1000, 2000))
            shootingStar = ShootingStarState(
                startX = Random.nextFloat(),
                startY = -0.1f,
                angle = Random.nextDouble(120.0, 150.0).toFloat(),
                speed = Random.nextFloat() * 0.8f + 0.5f,
                tailLength = Random.nextFloat() * 200f + 150f
            )
            shootingStarProgress.snapTo(0f)
            shootingStarProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = Random.nextInt(700, 1500), easing = LinearEasing)
            )
            shootingStar = null
        }
    }

    // 5. Se dibujan el fondo y todos los elementos celestes.
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF00001a), // Azul muy oscuro
                        Color(0xFF0c0c2b), // Azul noche
                        Color(0xFF1a1a3d)  // Púrpura oscuro
                    )
                )
            )
    ) {
        // Dibuja las estrellas que parpadean.
        stars.forEachIndexed { index, star ->
            val animatedAlpha = animatables[index].value

            // CORRECCIÓN: Se interpola el color basado en el brillo de la estrella.
            // Cuando la estrella está más brillante (alfa más alto), tiende a amarillo.
            val fraction = ((animatedAlpha - 0.2f) / (star.baseAlpha - 0.2f)).coerceIn(0f, 1f)
            val yellowTint = Color(0xFFFFF5B2) // Un amarillo suave
            val currentColor = lerp(Color.White, yellowTint, fraction)

            drawCircle(
                color = currentColor, // Se usa el color animado.
                center = Offset(star.x * size.width, star.y * size.height),
                radius = star.radius,
                alpha = animatedAlpha
            )
        }

        // Dibuja la estrella fugaz si está visible.
        shootingStar?.let { star ->
            val progress = shootingStarProgress.value
            if (progress > 0) {
                val angleRad = Math.toRadians(star.angle.toDouble()).toFloat()
                val distance = size.height * 1.5f * star.speed
                val currentX = star.startX * size.width + cos(angleRad) * distance * progress
                val currentY = star.startY * size.height + sin(angleRad) * distance * progress
                val tailX = currentX - cos(angleRad) * star.tailLength
                val tailY = currentY - sin(angleRad) * star.tailLength
                val headPosition = Offset(currentX, currentY)
                val tailPosition = Offset(tailX, tailY)
                drawLine(
                    brush = Brush.linearGradient(colors = listOf(Color.White, Color.Transparent), start = headPosition, end = tailPosition),
                    start = headPosition, end = tailPosition, strokeWidth = 2.5f
                )
                drawCircle(color = Color.White, center = headPosition, radius = 3.5f)
            }
        }
    }
}



// --- NUEVO: Detector de Agitado ---
@Composable
fun ShakeToReveal(
    threshold: Float = 15f,
    onShake: () -> Unit
) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var lastUpdate: Long = 0
    var lastX: Float = 0f
    var lastY: Float = 0f
    var lastZ: Float = 0f

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val currentTime = System.currentTimeMillis()
                    if ((currentTime - lastUpdate) > 200) { // Solo comprueba cada 200ms
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]
                        val speed = abs(x + y + z - lastX - lastY - lastZ) / (currentTime - lastUpdate) * 10000

                        if (speed > threshold) {
                            onShake()
                        }

                        lastUpdate = currentTime
                        lastX = x
                        lastY = y
                        lastZ = z
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* No-op */ }
        }
    }

    DisposableEffect(Unit) {
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }
}

/**
 * Representa una partícula de estrella para la animación.
 * @param x Posición horizontal relativa (-1.0 a 1.0).
 * @param y Posición vertical relativa (-1.0 a 1.0).
 * @param radius El tamaño de la estrella.
 * @param phaseOffset Un desfase para que cada estrella brille en un momento diferente.
 */
data class StarParticle(val x: Float, val y: Float, val radius: Float, val phaseOffset: Float)

@Composable
fun AnimatedBedIcon(modifier: Modifier = Modifier) {
    // Usamos 'remember' para que las estrellas de cada item de la lista tengan posiciones
    // aleatorias pero consistentes durante las recomposiciones.
    val stars = remember {
        List(12) { // 12 estrellas por ícono
            StarParticle(
                x = (Random.nextFloat() - 0.5f) * 2f, // Rango de -1 a 1
                y = (Random.nextFloat() - 0.5f) * 2f, // Rango de -1 a 1
                radius = Random.nextFloat() * 1.6f + 0.8f, // Estrellas de varios tamaños
                phaseOffset = Random.nextFloat() // Desfase aleatorio para el parpadeo
            )
        }
    }

    // Una transición infinita para que la animación se ejecute constantemente.
    val infiniteTransition = rememberInfiniteTransition(label = "star_twinkle_transition")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f, // Progreso de 0 a 1
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star_twinkle_progress"
    )

    Box(
        modifier = modifier.size(52.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ícono de la Cama
        Icon(
            imageVector = Icons.Rounded.Bed,
            contentDescription = stringResource(id = R.string.dreams_icon_cd),
            modifier = Modifier.size(36.dp),
            // Un color oscuro, casi negro, como pediste.
            tint = NightSkyStart.copy(alpha = 0.9f)
        )

        // Canvas para dibujar las estrellas animadas sobre el ícono
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2

            stars.forEach { star ->
                // Calculamos el brillo (alfa) usando una onda sinusoidal.
                // El 'phaseOffset' y 'animationProgress' hacen que cada estrella parpadee
                // a su propio ritmo, creando un efecto de centelleo natural.
                val wave = sin((animationProgress + star.phaseOffset) * 2 * Math.PI).toFloat()
                val alpha = (wave * 0.5f + 0.5f).coerceIn(0.1f, 1f) // Mapeamos de -1..1 a 0.1..1

                // Dibujamos cada estrella
                drawCircle(
                    color = GoldAccent,
                    radius = star.radius,
                    alpha = alpha,
                    center = Offset(
                        x = centerX + (star.x * centerX * 0.7f), // El 0.7f las mantiene más cerca del centro
                        y = centerY + (star.y * centerY * 0.7f)
                    )
                )
            }
        }
    }
}

//package com.matancita.loteria.ui.theme.screen
//
//import android.content.res.Configuration
//import androidx.compose.animation.animateContentSize
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.CubicBezierEasing
//import androidx.compose.animation.core.animateDpAsState
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.spring
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyListState
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowForward
//import androidx.compose.material.icons.filled.Clear
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.material.icons.rounded.Bedtime
//import androidx.compose.material.ripple.rememberRipple
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.ExperimentalComposeUiApi
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.blur
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.BlendMode
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.hapticfeedback.HapticFeedbackType
//import androidx.compose.ui.platform.*
//import androidx.compose.ui.res.stringArrayResource
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.TextUnit
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.matancita.loteria.R
//import kotlin.math.cos
//import kotlin.math.sin
//import kotlin.random.Random
//
//// --- Definiciones de Color de Ejemplo (reemplaza con las tuyas de Theme.kt si es necesario) ---
//val LuckyGreen = Color(0xFF1A5E20)
//val LightGreen = Color(0xFF8BC34A)
//val GoldAccent = Color(0xFFFFC107)
//
//fun Color.darken(factor: Float): Color {
//    val invertedFactor = (1 - factor).coerceIn(0f, 1f)
//    return Color(
//        red = this.red * invertedFactor,
//        green = this.green * invertedFactor,
//        blue = this.blue * invertedFactor,
//        alpha = this.alpha
//    )
//}
//
//data class DreamItem(val number: Int, val text: String, val meaning: String)
//
//@Composable
//fun rememberDreamList(): List<DreamItem> {
//    val dreamNames = stringArrayResource(id = R.array.dream_names)
//    val dreamMeanings = stringArrayResource(id = R.array.dream_meanings)
//
//    return remember(dreamNames, dreamMeanings) {
//        dreamNames.mapIndexed { index, name ->
//            DreamItem(
//                number = index + 1,
//                text = name,
//                meaning = dreamMeanings.getOrElse(index) { "" } // Seguridad por si los arrays no coinciden
//            )
//        }
//    }
//}
//
//
//@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
//@Composable
//fun Screen3Dreams() {
//    val allDreamExperiences = rememberDreamList() // Cargar la lista desde los recursos
//    var searchQuery by remember { mutableStateOf("") }
//    var selectedDreamNumber by remember { mutableStateOf<Int?>(null) }
//    val keyboardController = LocalSoftwareKeyboardController.current
//    val haptic = LocalHapticFeedback.current
//    val lazyListState = rememberLazyListState() // <-- NUEVO
//
//    val configuration = LocalConfiguration.current
//    val orientation = configuration.orientation
//
//    val desiredOrbSize = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
//        (configuration.screenWidthDp.dp * 0.33f).coerceIn(100.dp, 150.dp)
//    } else {
//        (configuration.screenHeightDp.dp * 0.45f).coerceIn(80.dp, 120.dp)
//    }
//
//    val filteredDreamItems by remember(searchQuery, allDreamExperiences) {
//        derivedStateOf {
//            if (searchQuery.isBlank()) {
//                allDreamExperiences
//            } else {
//                val queryLower = searchQuery.lowercase()
//                allDreamExperiences.filter { dreamItem ->
//                    dreamItem.text.lowercase().contains(queryLower) ||
//                            dreamItem.number.toString().contains(queryLower)
//                }
//            }
//        }
//    }
//
//    val onDreamItemClickLambda = { number: Int ->
//        if (selectedDreamNumber != number) {
//            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//        }
//        selectedDreamNumber = number
//        keyboardController?.hide()
//    }
//
//    // Cuando los resultados filtrados cambian, hacemos scroll al principio
//    LaunchedEffect(filteredDreamItems) {
//        if (filteredDreamItems.isNotEmpty()) {
//            lazyListState.animateScrollToItem(0)
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    listOf(
//                        LuckyGreen.copy(alpha = 0.2f),
//                        LightGreen.copy(alpha = 0.3f),
//                        LuckyGreen.copy(alpha = 0.25f)
//                    )
//                )
//            )
//    ) {
//        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
//            Screen3DreamsPortrait(
//                searchQuery = searchQuery,
//                onSearchQueryChange = { searchQuery = it },
//                selectedDreamNumber = selectedDreamNumber,
//                onDreamItemClick = { onDreamItemClickLambda(it) },
//                filteredDreamItems = filteredDreamItems,
//                desiredOrbSize = desiredOrbSize,
//                keyboardController = keyboardController,
//                lazyListState = lazyListState // <-- NUEVO
//            )
//        } else {
//            Screen3DreamsLandscape(
//                searchQuery = searchQuery,
//                onSearchQueryChange = { searchQuery = it },
//                selectedDreamNumber = selectedDreamNumber,
//                onDreamItemClick = { onDreamItemClickLambda(it) },
//                filteredDreamItems = filteredDreamItems,
//                desiredOrbSize = desiredOrbSize,
//                keyboardController = keyboardController,
//                lazyListState = lazyListState // <-- NUEVO
//            )
//        }
//    }
//}
//
//@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
//@Composable
//fun Screen3DreamsPortrait(
//    searchQuery: String,
//    onSearchQueryChange: (String) -> Unit,
//    selectedDreamNumber: Int?,
//    onDreamItemClick: (Int) -> Unit,
//    filteredDreamItems: List<DreamItem>,
//    desiredOrbSize: Dp,
//    keyboardController: SoftwareKeyboardController?,
//    lazyListState: LazyListState // <-- NUEVO
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(horizontal = 16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Spacer(modifier = Modifier.height(24.dp))
//
//        Text(
//            text = stringResource(id = R.string.dreams_oracle_title),
//            fontSize = 32.sp,
//            fontWeight = FontWeight.Bold,
//            fontFamily = FontFamily.Serif,
//            color = LuckyGreen.darken(0.1f),
//            modifier = Modifier.padding(bottom = 20.dp)
//        )
//
//        SelectedNumberDisplay(
//            selectedNumber = selectedDreamNumber,
//            orbSize = desiredOrbSize
//        )
//
//        OutlinedTextField(
//            value = searchQuery,
//            onValueChange = onSearchQueryChange,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 20.dp, bottom = 16.dp),
//            placeholder = { Text(stringResource(id = R.string.dreams_search_placeholder_long), color = LuckyGreen.copy(alpha = 0.7f)) },
//            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(id = R.string.dreams_search_cd), tint = LuckyGreen) },
//            trailingIcon = {
//                if (searchQuery.isNotEmpty()) {
//                    IconButton(onClick = { onSearchQueryChange("") }) {
//                        Icon(Icons.Filled.Clear, contentDescription = stringResource(id = R.string.dreams_clear_cd), tint = LuckyGreen)
//                    }
//                }
//            },
//            singleLine = true,
//            shape = RoundedCornerShape(28.dp),
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedTextColor = LuckyGreen.darken(0.2f),
//                unfocusedTextColor = LuckyGreen.darken(0.1f),
//                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
//                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
//                disabledContainerColor = Color.Transparent,
//                cursorColor = GoldAccent,
//                focusedBorderColor = GoldAccent.copy(alpha = 0.6f),
//                unfocusedBorderColor = LuckyGreen.copy(alpha = 0.4f),
//            ),
//            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
//            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
//        )
//
//        DreamListContent(
//            modifier = Modifier.weight(1f).padding(bottom = 16.dp),
//            filteredDreamItems = filteredDreamItems,
//            onDreamItemClick = onDreamItemClick,
//            selectedDreamNumber = selectedDreamNumber, // <--- Pásalo hacia abajo
//            lazyListState = lazyListState // <-- NUEVO
//        )
//    }
//}
//
//@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
//@Composable
//fun Screen3DreamsLandscape(
//    searchQuery: String,
//    onSearchQueryChange: (String) -> Unit,
//    selectedDreamNumber: Int?,
//    onDreamItemClick: (Int) -> Unit,
//    filteredDreamItems: List<DreamItem>,
//    desiredOrbSize: Dp,
//    keyboardController: SoftwareKeyboardController?,
//    lazyListState: LazyListState // <-- NUEVO
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(horizontal = 16.dp, vertical = 16.dp),
//        verticalAlignment = Alignment.Top
//    ) {
//        Column(
//            modifier = Modifier
//                .weight(0.45f)
//                .fillMaxHeight()
//                .padding(end = 8.dp)
//                .verticalScroll(rememberScrollState()),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = stringResource(id = R.string.dreams_oracle_title),
//                fontSize = 28.sp,
//                fontWeight = FontWeight.Bold,
//                fontFamily = FontFamily.Serif,
//                color = LuckyGreen.darken(0.1f),
//                textAlign = TextAlign.Center,
//                modifier = Modifier.padding(bottom = 16.dp)
//            )
//
//            SelectedNumberDisplay(
//                selectedNumber = selectedDreamNumber,
//                orbSize = desiredOrbSize
//            )
//
//            OutlinedTextField(
//                value = searchQuery,
//                onValueChange = onSearchQueryChange,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 16.dp),
//                placeholder = { Text(stringResource(id = R.string.dreams_search_placeholder_short), color = LuckyGreen.copy(alpha = 0.7f)) },
//                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(id = R.string.dreams_search_cd), tint = LuckyGreen) },
//                trailingIcon = {
//                    if (searchQuery.isNotEmpty()) {
//                        IconButton(onClick = { onSearchQueryChange("") }) {
//                            Icon(Icons.Filled.Clear, contentDescription = stringResource(id = R.string.dreams_clear_cd), tint = LuckyGreen)
//                        }
//                    }
//                },
//                singleLine = true,
//                shape = RoundedCornerShape(28.dp),
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedTextColor = LuckyGreen.darken(0.2f),
//                    unfocusedTextColor = LuckyGreen.darken(0.1f),
//                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
//                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
//                    disabledContainerColor = Color.Transparent,
//                    cursorColor = GoldAccent,
//                    focusedBorderColor = GoldAccent.copy(alpha = 0.6f),
//                    unfocusedBorderColor = LuckyGreen.copy(alpha = 0.4f),
//                ),
//                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
//                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
//            )
//        }
//
//        DreamListContent(
//            modifier = Modifier.weight(0.55f).fillMaxHeight(),
//            filteredDreamItems = filteredDreamItems,
//            onDreamItemClick = onDreamItemClick,
//            selectedDreamNumber = selectedDreamNumber, // <--- Pásalo hacia abajo
//            lazyListState = lazyListState // <-- NUEVO
//        )
//    }
//}
//
//@Composable
//fun DreamListContent(
//    modifier: Modifier = Modifier,
//    filteredDreamItems: List<DreamItem>,
//    onDreamItemClick: (Int) -> Unit,
//    lazyListState: LazyListState, // <-- NUEVO
//    selectedDreamNumber: Int? // <-- NUEVO: Para pasarlo al item
//) {
//    Box(
//        modifier = modifier
//            .fillMaxWidth(),
//        contentAlignment = Alignment.TopCenter
//    ) {
//        Box(modifier = Modifier.widthIn(max = 700.dp)) {
//            if (filteredDreamItems.isEmpty()) {
//                Column(
//                    modifier = Modifier.fillMaxSize().padding(top = 32.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Top
//                ) {
//                    Text(
//                        text = stringResource(id = R.string.dreams_no_results),
//                        fontSize = 17.sp,
//                        fontWeight = FontWeight.Medium,
//                        color = LuckyGreen.copy(alpha = 0.9f),
//                        textAlign = TextAlign.Center
//                    )
//                }
//            } else {
//                LazyColumn(
//                    verticalArrangement = Arrangement.spacedBy(14.dp),
//                    contentPadding = PaddingValues(vertical = 8.dp),
//                    state = lazyListState, // <-- NUEVO
//                ) {
//                    items(filteredDreamItems, key = { it.number }) { dreamItem ->
//                        DreamListItemRedesigned(
//                            dreamItem = dreamItem,
//                            isSelected = dreamItem.number == selectedDreamNumber, // <-- NUEVO
//                            onItemClick = onDreamItemClick
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun SelectedNumberDisplay(
//    selectedNumber: Int?,
//    orbSize: Dp = 120.dp
//) {
//    val density = LocalDensity.current
//
//    // Estados para controlar las fases de la animación de revelación
//    var animationState by remember(selectedNumber) { mutableStateOf(if (selectedNumber == null) "IDLE" else "REVEALED") }
//    var particles by remember { mutableStateOf<List<Particle>>(emptyList()) }
//
//    // Animaciones
//    val animatableScale = remember { Animatable(1.0f) }
//    val numberAlpha by animateFloatAsState(
//        targetValue = if (animationState == "REVEALED") 1f else 0f,
//        animationSpec = tween(durationMillis = 400, delayMillis = 200),
//        label = "numberAlpha"
//    )
//    val particleAlpha by animateFloatAsState(
//        targetValue = if (animationState == "REVEALING") 1f else 0f,
//        animationSpec = tween(durationMillis = 300, easing = CubicBezierEasing(0f, 1f, 0.5f, 1f)),
//        label = "particleAlpha"
//    )
//
//    // Dispara la secuencia de animación cuando el número cambia
//    LaunchedEffect(selectedNumber) {
//        if (selectedNumber != null) {
//            animationState = "REVEALING"
//            // Generar partículas aleatorias para el efecto
//            particles = List(60) {
//                Particle(
//                    x = Random.nextFloat(),
//                    y = Random.nextFloat(),
//                    speed = Random.nextFloat() * 1.5f + 0.5f,
//                    size = Random.nextFloat() * 1.5f + 1f
//                )
//            }
//            // Secuencia: escala abajo, escala arriba, revelar
//            animatableScale.animateTo(0.9f, spring(0.4f, 600f))
//            kotlinx.coroutines.delay(400) // Tiempo para el efecto de partículas
//            animationState = "REVEALED"
//            animatableScale.animateTo(1.2f, spring(0.4f, 450f))
//            animatableScale.animateTo(1.05f, spring(0.5f, 250f))
//        }
//    }
//
//    val numberFontSize: TextUnit = (orbSize.value * 0.42f).sp
//    val placeholderFontSize: TextUnit = (orbSize.value * 0.32f).sp
//
//    Box(
//        contentAlignment = Alignment.Center,
//        modifier = Modifier
//            .size(orbSize)
//            .graphicsLayer {
//                shadowElevation = with(density) { (orbSize.value * 0.125f).dp.toPx() }
//                shape = CircleShape
//                clip = true
//                scaleX = animatableScale.value
//                scaleY = animatableScale.value
//            }
//    ) {
//        // ... El código de dibujo del fondo y borde del orbe no cambia ...
//        // (Se ha copiado de tu código original para que sea completo)
//        Box(modifier = Modifier.matchParentSize().background(brush = Brush.radialGradient(colors = listOf(GoldAccent.copy(alpha = 0.7f), GoldAccent.copy(alpha = 0.9f), GoldAccent.darken(0.1f)), radius = with(density) { orbSize.toPx() }), shape = CircleShape).blur(radius = (orbSize.value * 0.025f).dp))
//        Canvas(modifier = Modifier.matchParentSize()) { /* ... */ }
//
//        // --- NUEVO: Canvas para las partículas ---
//        Canvas(modifier = Modifier.matchParentSize().graphicsLayer { alpha = particleAlpha }) {
//            val center = this.center
//            particles.forEach { particle ->
//                val angle = particle.x * 2 * Math.PI
//                val currentRadius = size.minDimension / 2 * particle.y * (1 - particleAlpha) * particle.speed
//                val xPos = center.x + cos(angle).toFloat() * currentRadius
//                val yPos = center.y + sin(angle).toFloat() * currentRadius
//                drawCircle(
//                    color = GoldAccent,
//                    radius = particle.size * (1 - particleAlpha) * 3f,
//                    center = Offset(xPos, yPos),
//                    alpha = (sin(particle.y * Math.PI)).toFloat() // Fade in/out based on position
//                )
//            }
//        }
//
//        // Número o Placeholder
//        Text(
//            text = selectedNumber?.toString()?.padStart(2, '0') ?: stringResource(id = R.string.dreams_orb_placeholder),
//            fontSize = if (selectedNumber != null) numberFontSize else placeholderFontSize,
//            fontWeight = FontWeight.ExtraBold,
//            fontFamily = FontFamily.SansSerif,
//            color = LuckyGreen.darken(0.3f),
//            modifier = Modifier.graphicsLayer { alpha = if (selectedNumber != null) numberAlpha else 1f }
//        )
//
//        // Reflejo de luz (no cambia)
//        if (selectedNumber != null) {
//            Canvas(modifier = Modifier.matchParentSize().graphicsLayer { alpha = numberAlpha }) { /* ... */ }
//        }
//    }
//}
//
//// Data class auxiliar para las partículas
//data class Particle(val x: Float, val y: Float, val speed: Float, val size: Float)
//
//
//@Composable
//fun DreamListItemRedesigned(
//    dreamItem: DreamItem,
//    isSelected: Boolean,
//    onItemClick: (Int) -> Unit
//) {
//    // Animación para el borde y la elevación cuando un item es seleccionado
//    val elevation by animateDpAsState(if (isSelected) 12.dp else 6.dp, label = "elevation")
//    val borderBrush = remember(isSelected) {
//        if (isSelected) {
//            Brush.linearGradient(colors = listOf(GoldAccent, GoldAccent.darken(0.3f)))
//        } else {
//            Brush.linearGradient(colors = listOf(LuckyGreen.copy(alpha = 0.3f), GoldAccent.copy(alpha = 0.2f), LuckyGreen.copy(alpha = 0.2f)))
//        }
//    }
//
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(
//                interactionSource = remember { MutableInteractionSource() },
//                indication = rememberRipple(bounded = true, color = GoldAccent.copy(alpha = 0.5f)),
//                onClick = { onItemClick(dreamItem.number) }
//            )
//            .padding(vertical = 2.dp),
//        shape = RoundedCornerShape(20.dp),
//        color = MaterialTheme.colorScheme.surfaceContainerHighest,
//        border = BorderStroke(
//            width = if (isSelected) 2.dp else 1.5.dp,
//            brush = borderBrush
//        ),
//        shadowElevation = elevation
//    ) {
//        // ==================================================================
//        // ---          CONTENIDO RESTAURADO QUE FALTABA AQUÍ           ---
//        // ==================================================================
//        Row(
//            modifier = Modifier
//                .padding(horizontal = 16.dp, vertical = 20.dp)
//                .animateContentSize(),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(18.dp)
//        ) {
//            Box(
//                contentAlignment = Alignment.Center,
//                modifier = Modifier
//                    .size(52.dp)
//                    .clip(CircleShape)
//                    .background(
//                        brush = Brush.radialGradient(
//                            colors = listOf(
//                                GoldAccent.copy(alpha = 0.4f),
//                                LuckyGreen.copy(alpha = 0.1f),
//                                Color.Transparent
//                            ),
//                            radius = 52f * 0.8f
//                        )
//                    )
//                    .border(1.dp, GoldAccent.copy(alpha = 0.3f), CircleShape)
//            ) {
//                Icon(
//                    imageVector = Icons.Rounded.Bedtime,
//                    contentDescription = stringResource(id = R.string.dreams_icon_cd),
//                    tint = GoldAccent.darken(0.1f),
//                    modifier = Modifier
//                        .size(34.dp)
//                        .graphicsLayer {
//                            rotationZ = if (dreamItem.number % 2 == 0) -7f else 7f
//                            alpha = 0.95f
//                        }
//                )
//                Box(modifier = Modifier.fillMaxSize()) {
//                    Box(
//                        modifier = Modifier
//                            .size(4.dp)
//                            .background(GoldAccent.copy(alpha = 0.3f), CircleShape)
//                            .align(Alignment.TopStart)
//                            .offset(x = 6.dp, y = 6.dp)
//                    )
//                    Box(
//                        modifier = Modifier
//                            .size(3.dp)
//                            .background(GoldAccent.copy(alpha = 0.3f), CircleShape)
//                            .align(Alignment.BottomEnd)
//                            .offset(x = (-6).dp, y = (-6).dp)
//                    )
//                }
//            }
//
//            Column(
//                modifier = Modifier.weight(1f),
//                verticalArrangement = Arrangement.spacedBy(4.dp)
//            ) {
//                Text(
//                    text = dreamItem.text,
//                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
//                    color = MaterialTheme.colorScheme.onSurface,
//                    lineHeight = 22.sp,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    modifier = Modifier.animateContentSize()
//                )
//                Text(
//                    text = dreamItem.meaning,
//                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    lineHeight = 20.sp,
//                    maxLines = 3,
//                    overflow = TextOverflow.Ellipsis,
//                    modifier = Modifier.animateContentSize()
//                )
//            }
//
//            Icon(
//                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
//                contentDescription = stringResource(id = R.string.dreams_details_cd),
//                tint = GoldAccent.copy(alpha = 0.8f),
//                modifier = Modifier.size(24.dp)
//            )
//        }
//    }
//}