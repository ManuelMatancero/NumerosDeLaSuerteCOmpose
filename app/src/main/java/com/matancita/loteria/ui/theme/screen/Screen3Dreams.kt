package com.matancita.loteria.ui.theme.screen

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Bed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matancita.loteria.R
import com.matancita.loteria.anuncios.AdvancedNativeAdView
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// --- Colores ---
val NightSkyStart = Color(0xFF0D1117)
val NightSkyEnd = Color(0xFF161B22)
val LuckyGreen = Color(0xFF2E7D32)
val GoldAccent = Color(0xFFFFC107)

// --- Modelos ---
data class DreamItem(val number: Int, val text: String, val meaning: String)
private data class Star(val x: Float, val y: Float, val radius: Float, val alpha: Float)

// --- Helper para Datos ---
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

// --- Pantalla Principal ---
@Composable
fun Screen3Dreams() {
    val allDreamExperiences = rememberDreamList()
    var searchQuery by remember { mutableStateOf("") }
    var selectedDream by remember { mutableStateOf<DreamItem?>(null) }

    // Estado para controlar si el orbe está "pensando"
    var isThinking by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

    val filteredDreamItems by remember(searchQuery, allDreamExperiences) {
        derivedStateOf {
            if (searchQuery.isBlank()) allDreamExperiences
            else {
                val queryLower = searchQuery.lowercase()
                allDreamExperiences.filter {
                    it.text.lowercase().contains(queryLower) || it.number.toString().contains(queryLower)
                }
            }
        }
    }

    // Función unificada para seleccionar sueño (desde lista o aleatorio)
    val selectDream = { dream: DreamItem ->
        isThinking = true // Inicia animación del orbe
        selectedDream = dream
        keyboardController?.hide()
    }

    // Scroll automático al seleccionar
    LaunchedEffect(selectedDream) {
        if (selectedDream != null) {
            // Pequeña pausa para simular "proceso mágico"
            delay(600)
            isThinking = false

            val index = filteredDreamItems.indexOf(selectedDream)
            if (index != -1) {
                lazyListState.animateScrollToItem(index)
            }
        }
    }

    // Detector de Agitado (Feature oculta/divertida)
    ShakeToReveal(threshold = 400f, onShake = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        searchQuery = ""
        selectDream(allDreamExperiences.random())
    })

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
        StarryNightBackground()

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            ScreenContentPortrait(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedDream = selectedDream,
                isThinking = isThinking,
                onDreamItemClick = {selectDream},
                onOrbClick = {
                    // Al tocar el orbe, elige uno al azar
                    searchQuery = ""
                    selectDream(allDreamExperiences.random())
                },
                filteredDreamItems = filteredDreamItems,
                lazyListState = lazyListState
            )
        } else {
            // Implementación Landscape simplificada para brevedad, usando la lógica Portrait como base
            ScreenContentPortrait(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedDream = selectedDream,
                isThinking = isThinking,
                onDreamItemClick = {selectDream},
                onOrbClick = {
                    searchQuery = ""
                    selectDream(allDreamExperiences.random())
                },
                filteredDreamItems = filteredDreamItems,
                lazyListState = lazyListState
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ScreenContentPortrait(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedDream: DreamItem?,
    isThinking: Boolean,
    onDreamItemClick: (DreamItem) -> Unit,
    onOrbClick: () -> Unit,
    filteredDreamItems: List<DreamItem>,
    lazyListState: LazyListState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- CABECERA ---
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Oráculo de Sueños",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // --- ORBE INTERACTIVO (EL CENTRO DE ATENCIÓN) ---
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 12.dp)) {
            OracleOrb(
                selectedNumber = selectedDream?.number,
                isThinking = isThinking,
                onClick = onOrbClick
            )
        }

        // --- TEXTO DE AYUDA CONTEXTUAL ---
        AnimatedContent(
            targetState = selectedDream,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "Hint Text"
        ) { dream ->
            if (dream == null) {
                Text(
                    text = "Toca el orbe para revelar tu suerte\no busca tu sueño abajo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tu número de la suerte para:",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldAccent.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "\"${dream.text}\"",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- BARRA DE BÚSQUEDA ---
        GlassySearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // --- LISTA DE SUEÑOS ---
        DreamListContent(
            modifier = Modifier.weight(1f),
            filteredDreamItems = filteredDreamItems,
            selectedDreamNumber = selectedDream?.number,
            onDreamItemClick = onDreamItemClick,
            lazyListState = lazyListState
        )
    }
}

// --- ORBE MEJORADO ---
@Composable
fun OracleOrb(
    selectedNumber: Int?,
    isThinking: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    // Animación de respiración cuando está inactivo
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "breath"
    )

    // Escala reactiva al toque
    val scale = if (isPressed) 0.9f else if (isThinking) 1.1f else breathScale

    val orbSize = 140.dp

    // Rotación para efecto de "pensando"
    val rotation by animateFloatAsState(
        targetValue = if (isThinking) 360f else 0f,
        animationSpec = if (isThinking) infiniteRepeatable(tween(1000, easing = LinearEasing)) else tween(0),
        label = "rotation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(orbSize)
            .scale(scale)
            .clip(CircleShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6C63FF).copy(alpha = 0.2f),
                        Color(0xFF2A2A72).copy(alpha = 0.6f),
                        Color.Transparent
                    )
                )
            )
    ) {
        // Anillos Giratorios (Efecto Mágico)
        if (isThinking) {
            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = rotation }) {
                drawCircle(
                    brush = Brush.sweepGradient(listOf(Color.Transparent, GoldAccent, Color.Transparent)),
                    radius = size.minDimension / 2.2f,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }

        // Núcleo del Orbe
        Box(
            modifier = Modifier
                .size(orbSize * 0.85f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4A90E2),
                            Color(0xFF003366)
                        ),
                        center = Offset.Unspecified,
                        radius = 200f
                    ),
                    shape = CircleShape
                )
                .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
        ) {
            // Brillo superior
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent),
                            startY = 0f,
                            endY = size.height * 0.5f
                        )
                        onDrawBehind { drawCircle(brush) }
                    }
            )
        }

        // Contenido Central (Número o Ícono)
        AnimatedContent(
            targetState = if (isThinking) "THINK" else selectedNumber,
            transitionSpec = { scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut() },
            label = "OrbContent"
        ) { state ->
            if (state == "THINK") {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(48.dp)
                )
            } else if (state is Int) {
                Text(
                    text = state.toString().padStart(2, '0'),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 10f))
                )
            } else {
                // Estado inicial (Signo de interrogación o icono místico)
                Text(
                    text = "?",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// --- BUSCADOR GLASSMORPHIC ---
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GlassySearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    BasicTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
        cursorBrush = SolidColor(GoldAccent),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(Color.White.copy(alpha = 0.1f)) // Fondo translúcido
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(25.dp))
    ) { innerTextField ->
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (searchQuery.isEmpty()) {
                    Text("Buscar sueño (ej. Boda, 15...)", color = Color.White.copy(alpha = 0.4f))
                }
                innerTextField()
            }
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}


// --- LISTA DE SUEÑOS ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DreamListContent(
    modifier: Modifier = Modifier,
    filteredDreamItems: List<DreamItem>,
    selectedDreamNumber: Int?,
    onDreamItemClick: (DreamItem) -> Unit,
    lazyListState: LazyListState
) {
    if (filteredDreamItems.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No encontramos ese sueño.\n¡Intenta con otra palabra!",
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            state = lazyListState,
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredDreamItems, key = { it.number }) { dreamItem ->
                GlassyDreamItem(
                    dreamItem = dreamItem,
                    isSelected = dreamItem.number == selectedDreamNumber,
                    onClick = { onDreamItemClick(dreamItem) },
                    modifier = Modifier.animateItem(
                        placementSpec = spring(stiffness = Spring.StiffnessLow)
                    )
                )
                // Insertar anuncio cada 5 items (ejemplo simple)
                if (dreamItem.number % 10 == 0) {
                    AdvancedNativeAdView()
                }
            }
        }
    }
}

@Composable
fun GlassyDreamItem(
    dreamItem: DreamItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) GoldAccent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
        label = "bg_color"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.1f),
        label = "border_color"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono animado (Usamos el que creamos antes o una versión simple)
        Box(modifier = Modifier
            .size(40.dp)
            .background(Color.Black.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
            // Muestra el número aquí para referencia rápida
            Text(
                text = dreamItem.number.toString(),
                color = if(isSelected) GoldAccent else Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dreamItem.text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = dreamItem.meaning,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                maxLines = if (isSelected) 4 else 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// --- UTILIDADES (Copiadas para compatibilidad) ---
@Composable
fun StarryNightBackground(starCount: Int = 150) {
    val stars = remember {
        List(starCount) {
            Star(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 1.5f + 0.5f, Random.nextFloat() * 0.7f + 0.2f)
        }
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEach { star ->
            drawCircle(Color.White, radius = star.radius, center = Offset(star.x * size.width, star.y * size.height), alpha = star.alpha)
        }
    }
}

@Composable
fun ShakeToReveal(threshold: Float = 15f, onShake: () -> Unit) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    var lastUpdate: Long = 0
    var lastX = 0f; var lastY = 0f; var lastZ = 0f

    val listener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val curTime = System.currentTimeMillis()
                    if ((curTime - lastUpdate) > 200) {
                        val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
                        val speed = abs(x + y + z - lastX - lastY - lastZ) / (curTime - lastUpdate) * 10000
                        if (speed > threshold) onShake()
                        lastUpdate = curTime; lastX = x; lastY = y; lastZ = z
                    }
                }
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
    }
    DisposableEffect(Unit) {
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }
}