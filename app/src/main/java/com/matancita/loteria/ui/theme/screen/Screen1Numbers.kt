package com.matancita.loteria.ui.theme.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matancita.loteria.R
import com.matancita.loteria.anuncios.AdvancedNativeAdView
import com.matancita.loteria.anuncios.InterstitialAdManager
import com.matancita.loteria.anuncios.TEST_INTERSTITIAL_AD_UNIT_ID
import com.matancita.loteria.ui.theme.DisabledButtonColor
import com.matancita.loteria.ui.theme.GoldAccent
import com.matancita.loteria.viewmodel.NumbersViewModel
import com.matancita.loteria.viewmodel.UserDataViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// --- Composable del Fondo Estrellado (integrado desde el contexto anterior) ---

// --- Pantalla Principal Rediseñada ---

private const val REVEAL_DURATION_MS = 1500L
private const val SETTLE_STAGGER_DELAY_MS = 400L
private const val VIBRATION_INTERVAL_MS = 150L
private const val SHOW_AD = true


@Composable
fun Screen1Numbers(
    numbersViewModel: NumbersViewModel,
    userDataViewModel: UserDataViewModel
) {
    val numbersDataState by numbersViewModel.screen1NumbersData.collectAsState()
    val canGenerate by numbersViewModel.canGenerateScreen1.collectAsState()
    val userProfile by userDataViewModel.userProfile.collectAsState()

    val displayedNumbers = remember { mutableStateListOf("?", "?", "?") }
    var isAnimating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
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

    LaunchedEffect(numbersDataState, canGenerate, isAnimating) {
        if (!isAnimating) {
            if (numbersDataState != null && !canGenerate) {
                numbersDataState?.numbers?.forEachIndexed { index, num ->
                    if (index < displayedNumbers.size) {
                        displayedNumbers[index] = num.toString().padStart(2, '0')
                    }
                }
            } else if (canGenerate) {
                for (i in 0 until displayedNumbers.size) {
                    displayedNumbers[i] = "?"
                }
            }
        }
    }

    var previousNumbersDataTimestamp by remember { mutableStateOf(0L) }
    LaunchedEffect(numbersDataState) {
        val currentData = numbersDataState
        if (currentData != null && !canGenerate && currentData.timestamp > previousNumbersDataTimestamp) {
            isAnimating = true
            previousNumbersDataTimestamp = currentData.timestamp

            coroutineScope.launch {
                val actualNumbers = currentData.numbers
                val animationJobs = mutableListOf<Job>()
                val vibrationJob: Job?

                vibrationJob = launch {
                    while (isAnimating) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        delay(VIBRATION_INTERVAL_MS)
                    }
                }

                for (i in 0 until displayedNumbers.size) {
                    val job = launch {
                        displayedNumbers[i] = "..." // Indicador de "conjurando"
                        delay(REVEAL_DURATION_MS + (i * SETTLE_STAGGER_DELAY_MS))
                        if(i < actualNumbers.size) {
                            displayedNumbers[i] = actualNumbers[i].toString().padStart(2, '0')
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    }
                    animationJobs.add(job)
                }

                animationJobs.joinAll()
                vibrationJob.cancelAndJoin()
                isAnimating = false
            }
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        StarryNightBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            val profile = userProfile
            val titleText = if (profile != null && profile.name.isNotBlank()) {
                val nameParts = profile.name.split(" ")
                val displayName = if (nameParts.isNotEmpty() && nameParts[0].length <= 12) nameParts[0] else profile.name.take(12) + "..."
                stringResource(R.string.lucky_numbers_title_user, displayName)
            } else {
                stringResource(R.string.lucky_numbers_title_default)
            }
            Text(
                text = titleText,
                fontSize = if (titleText.length > 25) 26.sp else 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(0.9f)
            )

            Text(
                text = stringResource(R.string.fortune_subtitle),
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Divider(
                color = GoldAccent.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth(0.6f).padding(bottom = 24.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                displayedNumbers.forEach { numberString ->
                    StarlightOrb(
                        number = numberString,
                        isAnimating = isAnimating && numberString == "..."
                    )
                }
            }

            Divider(
                color = GoldAccent.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth(0.6f).padding(top = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            var numbersGenerated by remember { mutableStateOf(false) }
            if (numbersGenerated) {
                LaunchedEffect(Unit) {
                    delay(3000)
                    showInterstitialTrigger = true
                    numbersGenerated = false
                }
            }

            Button(
                onClick = {
                    if (!isAnimating) {
                        userProfile?.let {
                            numbersViewModel.generateNumbersForScreen("screen1", 3, it)
                            numbersGenerated = true
                        }
                    }
                },
                enabled = canGenerate && userProfile != null && !isAnimating,
                modifier = Modifier.fillMaxWidth(0.85f).height(60.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A148C).copy(alpha = 0.6f),
                    contentColor = Color.White,
                    disabledContainerColor = DisabledButtonColor.copy(alpha = 0.3f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    text = when {
                        isAnimating -> stringResource(R.string.button_state_revealing)
                        canGenerate -> stringResource(R.string.button_state_reveal)
                        else -> stringResource(R.string.button_state_come_back)
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))
            if(SHOW_AD){
                AdvancedNativeAdView()
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StarlightOrb(
    number: String,
    isAnimating: Boolean,
    modifier: Modifier = Modifier,
    orbSize: Dp = 85.dp,
    fontSize: TextUnit = 30.sp
) {
    val density = LocalDensity.current
    val alpha by animateFloatAsState(targetValue = if (number == "?") 0.6f else 1f, label = "orbAlpha")
    val stardustProgress = remember { Animatable(0f) }

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

    LaunchedEffect(isAnimating) {
        if(isAnimating) {
            stardustProgress.animateTo(1f, animationSpec = infiniteRepeatable(
                tween(1000), RepeatMode.Restart
            ))
        } else {
            stardustProgress.snapTo(0f)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(orbSize)
            .alpha(alpha)
            .drawWithCache {
                val orbBrush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE1BEE7).copy(alpha = 0.4f),
                        Color(0xFF9C27B0).copy(alpha = 0.5f),
                        Color(0xFF4A148C).copy(alpha = 0.6f)
                    ),
                    radius = with(density) { (orbSize / 2).toPx() } * 1.5f
                )
                val borderBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color(0xFFE1BEE7).copy(alpha = 0.4f)
                    )
                )

                onDrawBehind {
                    drawCircle(brush = orbBrush)
                    drawCircle(brush = borderBrush, style = Stroke(width = 2.dp.toPx()))

                    if(isAnimating) {
                        val path = Path()
                        val center = Offset(size.width / 2, size.height / 2)
                        val maxRadius = size.width / 2.5f
                        val angle = stardustProgress.value * 360f * 3
                        val radius = stardustProgress.value * maxRadius
                        val x = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * radius
                        val y = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * radius
                        drawCircle(Color.White.copy(alpha = 1 - stardustProgress.value), radius = 3.dp.toPx(), center = Offset(x,y))
                    }
                }
            }
    ) {
        if (number != "...") {
            Text(
                text = number,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = 0.9f),
                fontFamily = FontFamily.SansSerif
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
                    val alpha1 = (wave * 0.5f + 0.5f).coerceIn(0.1f, 1f) // Mapeamos de -1..1 a 0.1..1

                    // Dibujamos cada estrella
                    drawCircle(
                        color = GoldAccent,
                        radius = star.radius,
                        alpha = alpha1,
                        center = Offset(
                            x = centerX + (star.x * centerX * 0.7f), // El 0.7f las mantiene más cerca del centro
                            y = centerY + (star.y * centerY * 0.7f)
                        )
                    )
                }
            }
        }
    }
}


