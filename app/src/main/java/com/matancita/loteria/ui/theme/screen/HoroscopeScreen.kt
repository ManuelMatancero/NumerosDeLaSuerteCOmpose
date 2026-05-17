package com.matancita.loteria.ui.theme.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matancita.loteria.R
import com.matancita.loteria.anuncios.AdmobAdaptiveBanner
import com.matancita.loteria.anuncios.InterstitialAdManager
import com.matancita.loteria.anuncios.TEST_INTERSTITIAL_AD_UNIT_ID
import com.matancita.loteria.viewmodel.HoroscopeViewModel
import com.matancita.loteria.viewmodel.UserDataViewModel
import kotlin.math.sin
import kotlin.random.Random

// Definición local por si falla el import

// --- Datos de Constelaciones ---
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

@Composable
fun HoroscopeScreen(
    userDataViewModel: UserDataViewModel = viewModel(),
    horoscopeViewModel: HoroscopeViewModel = viewModel()
) {
    val zodiacSigns = getZodiacSignDetails()
    val userProfile by userDataViewModel.userProfile.collectAsState()
    val uiState by horoscopeViewModel.uiState.collectAsState()
    val horoscopeData = uiState.data

    val currentSignInfo = userProfile?.zodiacSign?.let { englishKey ->
        zodiacSigns.find { it.englishKey == englishKey }
    }

    var showSelectionDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var pendingSignKey by remember { mutableStateOf<String?>(null) }

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
                InterstitialAdManager.showAd(it) { /* Callback opcional */ }
            }
            showInterstitialTrigger = false
        }
    }

    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            profile.zodiacSign?.let {
                horoscopeViewModel.loadHoroscopeData(profile)
            }
        }
    }

    // --- CORRECCIÓN 1: Fondo Oscuro ---
    // Agregamos el background al Box principal para que las estrellas sean visibles
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0D1117), // Azul noche muy oscuro
                        Color(0xFF161B22)
                    )
                )
            )
    ) {
        StarryNightBackground()

        // Lógica de diálogos
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
                onDismiss = { if (userProfile?.zodiacSign != null) showSelectionDialog = false }
            )
        }

        if (showConfirmationDialog && pendingSignKey != null) {
            val newSignLocalizedName = zodiacSigns.find { it.englishKey == pendingSignKey }?.localizedName ?: ""
            MagicalConfirmationDialog(
                newSign = newSignLocalizedName,
                onConfirm = {
                    pendingSignKey?.let { userDataViewModel.saveZodiacSign(it) }
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (userProfile != null && horoscopeData != null && currentSignInfo != null) {
            val constellationPoints = currentSignInfo.constellationPoints

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                Text(
                    text = stringResource(R.string.horoscope_title),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    style = TextStyle(shadow = Shadow(Color.White.copy(alpha = 0.5f), blurRadius = 10f))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentSignInfo.localizedName.uppercase(),
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = TextStyle(brush = Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.8f), Color(0xFFC0C0C0))))
                )

                Spacer(modifier = Modifier.height(24.dp))

                ConstellationCanvas(
                    points = constellationPoints,
                    tappedStars = horoscopeData.tappedStars,
                    isComplete = horoscopeData.numbersRevealed,
                    onStarTap = { index ->
                        val totalStars = constellationPoints.size
                        if (index == horoscopeData.tappedStars.size) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        horoscopeViewModel.onStarTapped(index, totalStars)
                    if (index == totalStars - 1) {
                        showInterstitialTrigger = true
                    }
                    }
                )

                // Números revelados
                Box(modifier = Modifier.fillMaxWidth().height(90.dp), contentAlignment = Alignment.Center) {
                    if (horoscopeData.numbersRevealed) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            horoscopeData.luckyNumbers.forEach { number ->
                                CelestialNumber(number = number.toString().padStart(2, '0'))
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.horoscope_tap_stars_instruction),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
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
                            Icon(Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(stringResource(R.string.horoscope_change_sign))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tarjeta de Horóscopo
                Box(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .background(Color.Black.copy(alpha = 0.25f), shape = RoundedCornerShape(16.dp))
                        .border(
                            BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SelectionContainer {
                            Text(
                                text = uiState.translatedHoroscope
                                    ?: uiState.data?.dailyHoroscope
                                    ?: "",
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Serif,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp
                            )
                        }

                        if (uiState.isTranslating) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White.copy(alpha = 0.7f),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.horoscope_translating_hint),
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
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
                    }
                }

                }
                AdmobAdaptiveBanner(adUnitId = "ca-app-pub-9861862421891852/2370788758")
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ConstellationCanvas(
    points: List<Offset>,
    tappedStars: Set<Int>,
    isComplete: Boolean,
    onStarTap: (Int) -> Unit
) {
    val latestOnStarTap by rememberUpdatedState(onStarTap)
    val density = LocalDensity.current

    val infiniteTransition = rememberInfiniteTransition(label = "constellation")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(animation = tween(1200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "pulse"
    )

    val touchRadiusPx = with(density) { 48.dp.toPx() }
    val detectionRadiusPx = with(density) { 55.dp.toPx() }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .pointerInput(points, isComplete) {
                detectTapGestures { tapOffset ->
                    if (isComplete) return@detectTapGestures

                    var bestMatchIndex = -1
                    var minDistance = Float.MAX_VALUE

                    points.forEachIndexed { index, starOffset ->
                        val starPosition = Offset(starOffset.x * size.width, starOffset.y * size.height)
                        val dist = (tapOffset - starPosition).getDistance()

                        if (dist < detectionRadiusPx && dist < minDistance) {
                            minDistance = dist
                            bestMatchIndex = index
                        }
                    }

                    if (bestMatchIndex != -1) {
                        latestOnStarTap(bestMatchIndex)
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height

        if (tappedStars.size > 1) {
            for (i in 0 until tappedStars.size - 1) {
                if (i >= points.size || i + 1 >= points.size) break
                val start = Offset(points[i].x * width, points[i].y * height)
                val end = Offset(points[i + 1].x * width, points[i + 1].y * height)

                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.White.copy(alpha = 0.2f), Color.White, Color.White.copy(alpha = 0.2f))
                    ),
                    start = start,
                    end = end,
                    strokeWidth = if (isComplete) 4f else 2f,
                    cap = StrokeCap.Round
                )
            }
        }

        if (!isComplete && tappedStars.isNotEmpty() && tappedStars.size < points.size) {
            val lastIndex = tappedStars.size - 1
            val nextIndex = tappedStars.size
            val start = Offset(points[lastIndex].x * width, points[lastIndex].y * height)
            val end = Offset(points[nextIndex].x * width, points[nextIndex].y * height)

            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = start,
                end = end,
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        points.forEachIndexed { index, offset ->
            val center = Offset(offset.x * width, offset.y * height)
            val isTapped = index in tappedStars
            val isNextToTap = index == tappedStars.size && !isComplete
            val baseRadius = if (isTapped || isComplete) 12.dp.toPx() else 8.dp.toPx()

            if (isNextToTap) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = baseRadius * 2.5f * pulseScale,
                    center = center
                )
                drawCircle(
                    color = GoldAccent.copy(alpha = 0.6f),
                    radius = baseRadius * pulseScale,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.6f), Color.Transparent),
                    center = center,
                    radius = baseRadius * 3f
                ),
                radius = baseRadius * 3f,
                center = center
            )

            drawCircle(
                color = if (isTapped || isComplete) Color.White else Color.White.copy(alpha = 0.7f),
                radius = if (isTapped) baseRadius else baseRadius * 0.8f,
                center = center
            )

            if (isTapped || isComplete) {
                drawCircle(
                    color = Color(0xFFADDFFA),
                    radius = baseRadius * 0.5f,
                    center = center
                )
            }
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
    signsInfo: List<ZodiacSignInfo>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0c0c2b).copy(alpha = 0.95f),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
            .heightIn(max = 600.dp),
        title = {
            Text(
                stringResource(R.string.horoscope_select_sign_title),
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
            ) {
                items(signsInfo) { signInfo ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            // CORRECCIÓN 2: Uso seguro de Clickable en listas para evitar crash
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onSignSelected(signInfo.englishKey) }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = signInfo.localizedName,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.common_cancel), color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    )
}

// CORRECCIÓN 3: Completar las funciones auxiliares que faltaban al final

data class ZodiacSignInfo(
    val englishKey: String,
    val localizedName: String,
    val constellationPoints: List<Offset>
)

@Composable
fun getZodiacSignDetails(): List<ZodiacSignInfo> {
    val englishKeys = stringArrayResource(id = R.array.zodiac_signs_english_keys)
    val displayNames = stringArrayResource(id = R.array.zodiac_signs_display_names)

    val size = minOf(englishKeys.size, displayNames.size, constellationPointsData.size)

    return List(size) { index ->
        ZodiacSignInfo(
            englishKey = englishKeys[index],
            localizedName = displayNames[index],
            constellationPoints = constellationPointsData[index]
        )
    }
}