package com.matancita.loteria.ui.theme.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matancita.loteria.R
import com.matancita.loteria.anuncios.AdmobAdaptiveBanner
import com.matancita.loteria.anuncios.AdvancedNativeAdView
import com.matancita.loteria.anuncios.InterstitialAdManager
import com.matancita.loteria.anuncios.TEST_INTERSTITIAL_AD_UNIT_ID
import com.matancita.loteria.ui.theme.DisabledButtonColor
import com.matancita.loteria.ui.theme.GoldAccent
import com.matancita.loteria.viewmodel.HoroscopeViewModel
import com.matancita.loteria.viewmodel.UserDataViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random



// Define constellation points separately. The order must match the string-array.
val constellationPointsData = listOf(
    listOf(Offset(0.2f, 0.4f), Offset(0.4f, 0.3f), Offset(0.6f, 0.45f), Offset(0.8f, 0.6f)), // Aries
    listOf(Offset(0.2f, 0.5f), Offset(0.4f, 0.3f), Offset(0.6f, 0.4f), Offset(0.8f, 0.2f), Offset(0.5f, 0.7f)), // Tauro
    listOf(Offset(0.3f, 0.2f), Offset(0.35f, 0.8f), Offset(0.7f, 0.2f), Offset(0.75f, 0.8f)), // Géminis
    listOf(Offset(0.3f, 0.4f), Offset(0.5f, 0.5f), Offset(0.7f, 0.4f), Offset(0.5f, 0.7f)), // Cáncer
    listOf(Offset(0.2f, 0.5f), Offset(0.4f, 0.6f), Offset(0.6f, 0.5f), Offset(0.8f, 0.6f), Offset(0.5f, 0.3f)), // Leo
    listOf(Offset(0.2f, 0.3f), Offset(0.4f, 0.5f), Offset(0.6f, 0.4f), Offset(0.8f, 0.6f), Offset(0.7f, 0.8f)), // Virgo
    listOf(Offset(0.2f, 0.5f), Offset(0.4f, 0.4f), Offset(0.6f, 0.4f), Offset(0.8f, 0.5f), Offset(0.5f, 0.7f)), // Libra
    listOf(Offset(0.2f, 0.4f), Offset(0.4f, 0.5f), Offset(0.6f, 0.3f), Offset(0.8f, 0.4f), Offset(0.7f, 0.7f)), // Scorpio
    listOf(Offset(0.2f, 0.6f), Offset(0.4f, 0.4f), Offset(0.6f, 0.5f), Offset(0.8f, 0.3f), Offset(0.7f, 0.7f)), // Sagitario
    listOf(Offset(0.2f, 0.5f), Offset(0.4f, 0.3f), Offset(0.6f, 0.4f), Offset(0.8f, 0.6f), Offset(0.7f, 0.8f)), // Capricornio
    listOf(Offset(0.2f, 0.4f), Offset(0.4f, 0.6f), Offset(0.6f, 0.3f), Offset(0.8f, 0.5f)), // Acuario
    listOf(Offset(0.3f, 0.3f), Offset(0.35f, 0.7f), Offset(0.7f, 0.4f), Offset(0.75f, 0.8f))  // Piscis
)

// This function creates the map inside the Composable where resources are available.
//@Composable
//fun getZodiacConstellations(): Map<String, List<Offset>> {
//    val signNames = stringArrayResource(id = R.array.zodiac_signs)
//    // Associate each sign name with its corresponding list of points.
//    return signNames.zip(constellationPointsData).toMap()
//}


@Composable
fun  HoroscopeScreen(
    userDataViewModel: UserDataViewModel = viewModel(),
    horoscopeViewModel: HoroscopeViewModel = viewModel()
) {

    // 1. Obtén la lista completa de signos con sus detalles.
    val zodiacSigns = getZodiacSignDetails()

    val userProfile by userDataViewModel.userProfile.collectAsState()
    val uiState by horoscopeViewModel.uiState.collectAsState()
    val horoscopeData = uiState.data

    // 2. Encuentra el objeto del signo actual del usuario usando la clave en inglés.
    val currentSignInfo = userProfile?.zodiacSign?.let { englishKey ->
        zodiacSigns.find { it.englishKey == englishKey }
    }

    var showSelectionDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var pendingSignKey by remember { mutableStateOf<String?>(null) } // Ahora guardamos la clave
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val activity = LocalActivity.current

    var showInterstitialTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        InterstitialAdManager.loadAd(context, TEST_INTERSTITIAL_AD_UNIT_ID)
    }

    if (showInterstitialTrigger) {
        LaunchedEffect(Unit) {
            activity?.let {
                InterstitialAdManager.showAd(it) { /* Ad closed callback */ }
            }
            showInterstitialTrigger = false
        }
    }

    LaunchedEffect(userProfile) {
        userProfile?.zodiacSign?.let {
            horoscopeViewModel.loadHoroscopeData(userProfile!!)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        StarryNightBackground()

        if ((userProfile?.zodiacSign == null && userProfile != null) || showSelectionDialog) {
            MagicalZodiacSelectionDialog(
                signsInfo = zodiacSigns,
                onSignSelected = { selectedSign ->
                    showSelectionDialog = false
                    if (horoscopeData?.numbersRevealed == true && userProfile?.zodiacSign != selectedSign) {
                        pendingSignKey = selectedSign
                        showConfirmationDialog = true
                    } else {
                        userDataViewModel.saveZodiacSign(selectedSign)
                    }
                },
                onDismiss = { showSelectionDialog = false }
            )
        }

        if (showConfirmationDialog && pendingSignKey != null) {
            // 4. Busca el nombre localizado del nuevo signo para mostrarlo en la confirmación.
            val newSignLocalizedName = zodiacSigns.find { it.englishKey == pendingSignKey }?.localizedName ?: ""
            MagicalConfirmationDialog(
                newSign = newSignLocalizedName,
                onConfirm = {
                    userDataViewModel.saveZodiacSign(pendingSignKey!!)
                    showConfirmationDialog = false
                    pendingSignKey = null
                },
                onDismiss = {
                    showConfirmationDialog = false
                    pendingSignKey = null
                }
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else if (userProfile != null && horoscopeData != null && currentSignInfo != null) {
            val constellationPoints = currentSignInfo.constellationPoints // <-- CAMBIO

            // MODIFICACIÓN #1: Se añade el modificador "verticalScroll" a la columna principal.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()) // <-- PANTALLA SCROLLABLE
            ) {
                Text(
                    text = stringResource(R.string.horoscope_title),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    style = androidx.compose.ui.text.TextStyle(shadow = Shadow(Color.White.copy(alpha = 0.5f), blurRadius = 10f))
                )
                Spacer(modifier = Modifier.height(16.dp))
                userProfile!!.zodiacSign?.let { sign ->
                    Text(
                        text = currentSignInfo.localizedName.uppercase(),
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        style = androidx.compose.ui.text.TextStyle(brush = Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.8f), Color(0xFFC0C0C0))))
                    )
                }
                Spacer(modifier = Modifier.weight(0.5f))

                ConstellationCanvas(
                    points = constellationPoints,
                    tappedStars = horoscopeData.tappedStars,
                    isComplete = horoscopeData.numbersRevealed,
                    onStarTap = { index ->
                        val totalStars = constellationPoints.size
                        if (index == horoscopeData.tappedStars.size && index < totalStars) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        horoscopeViewModel.onStarTapped(index, totalStars)
                        if (index == totalStars - 1) {
                            showInterstitialTrigger = true
                        }
                    }
                )

                Box(modifier = Modifier.fillMaxWidth().height(90.dp), contentAlignment = Alignment.Center) {
                    if (horoscopeData.numbersRevealed) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            horoscopeData.luckyNumbers.forEach { number ->
                                CelestialNumber(number = number.toString().padStart(2, '0'))
                            }
                        }
                    }
                }
                Box(modifier = Modifier.height(60.dp), contentAlignment = Alignment.Center) {
                    if (horoscopeData.numbersRevealed) {
                        Button(
                            onClick = { showSelectionDialog = true },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Icon(Icons.Rounded.Sync, stringResource(R.string.horoscope_change_sign), modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(stringResource(R.string.horoscope_change_sign))
                        }

                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                // MODIFICACIÓN #2: Se mejora la presentación del texto del horóscopo.
                Box(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .background(Color.Black.copy(alpha = 0.25f), shape = RoundedCornerShape(16.dp))
                        .border(
                            BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SelectionContainer {
                            Text(
                                // Mostramos el texto traducido si existe, si no, el original.
                                text = uiState.translatedHoroscope
                                    ?: uiState.data?.dailyHoroscope
                                    ?: stringResource(R.string.horoscope_tap_stars_instruction),
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Serif,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center,
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mostramos el botón solo si hay un horóscopo que traducir
                        if (uiState.data?.dailyHoroscope?.isNotBlank() == true) {
                            // Si está traduciendo, muestra un indicador de carga
                            if (uiState.isTranslating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                OutlinedButton(
                                    onClick = { horoscopeViewModel.requestTranslation() },
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Translate,
                                        contentDescription = "Translate",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(
                                        // Cambia el texto del botón si ya está traducido
                                        text = if (uiState.translatedHoroscope == null) "Traducir" else "Ver Original",
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                        uiState.translationError?.let { error ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                        // DENTRO DEL Column PRINCIPAL, AL FINAL, DESPUÉS DEL BOX DEL HORÓSCOPO
                        Spacer(modifier = Modifier.height(16.dp))
                        AdmobAdaptiveBanner(adUnitId = "ca-app-pub-9861862421891852/2370788758") // Usa tu ID de banner
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ConstellationCanvas(
    points: List<Offset>, tappedStars: Set<Int>, isComplete: Boolean, onStarTap: (Int) -> Unit
) {
    val latestOnStarTap by rememberUpdatedState(onStarTap)
    val infiniteTransition = rememberInfiniteTransition(label = "constellation_pulse")
    val pulseAnimation by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.6f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "pulse_animation"
    )
    val masterClock by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(5000, easing = LinearEasing)),
        label = "master_clock"
    )
    val randomOffsets = remember(points) { points.map { Random.nextFloat() } }

    Canvas(
        modifier = Modifier.fillMaxWidth().height(300.dp).pointerInput(points) {
            detectTapGestures { tapOffset ->
                if (isComplete) return@detectTapGestures
                points.forEachIndexed { index, starOffset ->
                    val starPosition = Offset(starOffset.x * size.width, starOffset.y * size.height)
                    if ((tapOffset - starPosition).getDistance() < 45f) {
                        latestOnStarTap(index)
                    }
                }
            }
        }
    ) {
        if (tappedStars.size > 1) {
            for (i in 0 until tappedStars.size - 1) {
                if (i + 1 >= points.size) continue
                drawLine(
                    brush = Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.3f))),
                    start = Offset(points[i].x * size.width, points[i].y * size.height),
                    end = Offset(points[i + 1].x * size.width, points[i + 1].y * size.height),
                    strokeWidth = if (isComplete) 3.5f else 2.5f, cap = StrokeCap.Round
                )
            }
        }

        points.forEachIndexed { index, offset ->
            if (index >= randomOffsets.size) return@forEachIndexed
            val isTapped = index in tappedStars
            val isNextToTap = index == tappedStars.size
            val center = Offset(offset.x * size.width, offset.y * size.height)
            val scale = 1f + 0.1f * sin((masterClock + randomOffsets[index]) * (2 * Math.PI.toFloat()))
            val baseRadius = if (isTapped || isComplete) 24f else 20f
            val radius = baseRadius * scale

            if (isNextToTap && !isComplete) {
                drawCircle(brush = Brush.radialGradient(colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)), radius = radius * 2.0f * pulseAnimation, center = center)
            }

            drawCircle(
                brush = Brush.radialGradient(colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent), center = center, radius = radius * 2.5f),
                radius = radius * 2.5f, center = center
            )
            drawCircle(color = Color.White, radius = radius, center = center)
            drawCircle(color = Color(0xFFADDFFA), radius = radius / 1.8f, center = center)
        }
    }
}


@Composable
fun CelestialNumber(number: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(60.dp)
            .drawWithCache {
                val orbBrush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.3f), Color(0xFF89CFF0).copy(alpha = 0.4f), Color.Transparent),
                    radius = size.width / 2 * 1.5f
                )
                val borderBrush = Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.7f), Color(0xFF89CFF0).copy(alpha = 0.5f)))
                onDrawBehind {
                    drawCircle(brush = orbBrush)
                    drawCircle(brush = borderBrush, style = Stroke(width = 1.5.dp.toPx()))
                }
            }
    ) {
        Text(text = number, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
    }
}

@Composable
fun MagicalConfirmationDialog(newSign: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.horoscope_confirm_change_title), fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f)) },
        text = { Text(stringResource(R.string.horoscope_confirm_change_message, newSign), color = Color.White.copy(alpha = 0.8f)) },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))) { Text(
                stringResource(R.string.common_continue)
            ) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))) { Text(
                stringResource(R.string.common_cancel), color = Color.White.copy(alpha = 0.8f)) }
        },
        containerColor = Color(0xFF1A237E).copy(alpha = 0.8f),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
    )
}

@Composable
fun MagicalZodiacSelectionDialog(
    onSignSelected: (String) -> Unit,
    signsInfo: List<ZodiacSignInfo>, // <-- CAMBIO: Recibe la lista de objetos
    onDismiss: () -> Unit) {
    // Get the localized map.
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0c0c2b).copy(alpha = 0.9f),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
        title = { Text(stringResource(R.string.horoscope_select_sign_title), color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            LazyVerticalGrid(columns = GridCells.Fixed(3), contentPadding = PaddingValues(top = 16.dp)) {
                items(signsInfo) { signInfo ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            // Al hacer clic, devuelve la clave en inglés
                            .clickable { onSignSelected(signInfo.englishKey) }
                            .padding(8.dp)
                    ) {
                        // Aquí podrías usar un painterResource si tuvieras íconos
                        // Icon(painter = painterResource(id = R.drawable.ic_aries), ... )
                        Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(48.dp))
                        Text(
                            text = signInfo.localizedName,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.9f))
                ) { Text(stringResource(R.string.common_cancel)) }
            }
        }
    )
}

// Un modelo de datos para vincular la clave en inglés con el nombre localizado
data class ZodiacSignInfo(
    val englishKey: String, // ej: "Taurus"
    val localizedName: String, // ej: "Tauro"
    val constellationPoints: List<Offset>
)

// Nueva función para obtener la lista completa de signos con toda su información
@Composable
fun getZodiacSignDetails(): List<ZodiacSignInfo> {
    val englishKeys = stringArrayResource(id = R.array.zodiac_signs_english_keys)
    val displayNames = stringArrayResource(id = R.array.zodiac_signs_display_names)

    return englishKeys.mapIndexed { index, key ->
        ZodiacSignInfo(
            englishKey = key,
            localizedName = displayNames[index],
            constellationPoints = constellationPointsData[index]
        )
    }
}