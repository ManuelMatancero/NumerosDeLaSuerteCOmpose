package com.matancita.loteria.ui.theme.screen

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType // Import correcto
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matancita.loteria.R
import com.matancita.loteria.anuncios.AdmobAdaptiveBanner
import com.matancita.loteria.anuncios.AdvancedNativeAdView
import com.matancita.loteria.anuncios.InterstitialAdManager
import com.matancita.loteria.anuncios.TEST_ADAPTIVE_BANNER_AD_UNIT_ID
import com.matancita.loteria.anuncios.TEST_INTERSTITIAL_AD_UNIT_ID
import com.matancita.loteria.ui.theme.DisabledButtonColor
import com.matancita.loteria.ui.theme.GoldAccent
import com.matancita.loteria.ui.theme.LightGreen
import com.matancita.loteria.ui.theme.LuckyGreen
import com.matancita.loteria.viewmodel.NumbersViewModel
import com.matancita.loteria.viewmodel.UserDataViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

// Suponiendo que Color.darken() está disponible
/*
fun Color.darken(factor: Float): Color {
    val invertedFactor = (1 - factor).coerceIn(0f, 1f)
    return Color(
        red = this.red * invertedFactor,
        green = this.green * invertedFactor,
        blue = this.blue * invertedFactor,
        alpha = this.alpha
    )
}
*/

private const val SPIN_DURATION_BASE_MS = 1200L
private const val SPIN_DURATION_STAGGER_MS = 200L
private const val SPIN_INTERVAL_MS = 50L
private const val SETTLE_STAGGER_LAUNCH_DELAY_MS = 300L
private const val VIBRATION_INTERVAL_MS = 220L // Ajustar para la sensación deseada

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen1Numbers(
    numbersViewModel: NumbersViewModel,
    userDataViewModel: UserDataViewModel
) {
    val numbersDataState by numbersViewModel.screen1NumbersData.collectAsState()
    val canGenerate by numbersViewModel.canGenerateScreen1.collectAsState()
    val userProfile by userDataViewModel.userProfile.collectAsState()

    val displayedSlotNumbers = remember { mutableStateListOf("?", "?", "?") }
    var isAnimating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val numbersGeneratedState = remember { mutableStateOf(false) }

    //Interstitial AdManager
    val context = LocalContext.current
    val activity = LocalActivity.current // Obtener la Activity
    // Cargar el anuncio intersticial cuando la pantalla entra en composición (o cuando sea apropiado)
    LaunchedEffect(Unit) {
        // Usa tu ID de unidad de anuncio real aquí para producción
        InterstitialAdManager.loadAd(context, TEST_INTERSTITIAL_AD_UNIT_ID)
    }
    // Ejemplo de cómo podrías decidir mostrar el anuncio:
    // Supongamos que tienes un estado que cuenta las interacciones o una acción específica
    var showInterstitialTrigger by remember { mutableStateOf(false) } // Reemplaza con tu lógica
    if (showInterstitialTrigger) {
        LaunchedEffect(Unit) { // Usar LaunchedEffect para evitar múltiples llamadas en recomposiciones
            if (activity != null && InterstitialAdManager.isAdLoaded()) {
                InterstitialAdManager.showAd(activity) {
                    // Acción a realizar después de que el anuncio se cierre (o si no se muestra)

                    // Por ejemplo, navegar a otra pantalla, o simplemente continuar.
                }
            } else {

            }

        }
    }
    //FIN Interstitial ad //////


    // LaunchedEffect para visualización inicial (sin cambios)
    LaunchedEffect(numbersDataState, canGenerate, isAnimating) {
        if (!isAnimating) {
            if (numbersDataState != null && !canGenerate) {
                numbersDataState?.numbers?.forEachIndexed { index, num ->
                    if (index < displayedSlotNumbers.size) {
                        displayedSlotNumbers[index] = num.toString().padStart(2, '0')
                    }
                }
            } else if (canGenerate) {
                for (i in 0 until displayedSlotNumbers.size) {
                    displayedSlotNumbers[i] = "?"
                }
            }
        }
    }

    var previousNumbersDataTimestamp by remember { mutableStateOf(0L) }
    LaunchedEffect(numbersDataState, canGenerate) {
        val currentNumbersData = numbersDataState
        if (currentNumbersData != null && !canGenerate &&
            currentNumbersData.timestamp > previousNumbersDataTimestamp &&
            !isAnimating
        ) {
            isAnimating = true
            previousNumbersDataTimestamp = currentNumbersData.timestamp

            coroutineScope.launch {
                val actualNewNumbers = currentNumbersData.numbers
                val animationJobs = mutableListOf<Job>()
                var vibrationJob: Job? = null

                try {
                    vibrationJob = launch {
                        while (true) {
                            // MEJORA: Usar un tipo de HapticFeedbackType existente y ligero
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            delay(VIBRATION_INTERVAL_MS)
                        }
                    }

                    for (i in 0 until displayedSlotNumbers.size) {
                        if (i >= actualNewNumbers.size) break
                        val job = launch {
                            val spinDuration = SPIN_DURATION_BASE_MS + (i * SPIN_DURATION_STAGGER_MS)
                            val spinEndTime = System.currentTimeMillis() + spinDuration
                            while (System.currentTimeMillis() < spinEndTime) {
                                displayedSlotNumbers[i] = Random.nextInt(1, 101).toString().padStart(2, '0')
                                delay(SPIN_INTERVAL_MS)
                            }
                            displayedSlotNumbers[i] = actualNewNumbers[i].toString().padStart(2, '0')
                        }
                        animationJobs.add(job)

                        if (i < displayedSlotNumbers.size - 1) {
                            delay(SETTLE_STAGGER_LAUNCH_DELAY_MS)
                        }
                    }

                    animationJobs.joinAll()

                } finally {
                    vibrationJob?.cancelAndJoin()
                    isAnimating = false
                }
            }
        } else if (currentNumbersData == null && canGenerate) {
            previousNumbersDataTimestamp = 0L
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        LuckyGreen.copy(alpha = 0.2f),
                        LightGreen.copy(alpha = 0.3f),
                        LuckyGreen.copy(alpha = 0.25f)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        val profile = userProfile
        val titleText = if (profile != null && profile.name.isNotBlank()) {
            val nameParts = profile.name.split(" ")
            val displayName = if (nameParts.isNotEmpty() && nameParts[0].length <= 12) {
                nameParts[0]
            } else {
                profile.name.take(12) + if (profile.name.length > 12) "..." else ""
            }
            stringResource(R.string.lucky_numbers_title_user, displayName)
        } else {
            // IMPLEMENTACIÓN DE STRING POR DEFECTO
            stringResource(R.string.lucky_numbers_title_default)
        }
        val titleFontSize = if (titleText.length > 25) 26.sp else 30.sp
        Text(
            text = titleText,
            fontSize = titleFontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = LuckyGreen.darken(0.1f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth(0.9f)
        )

        Text(
            text = stringResource(R.string.fortune_subtitle),
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            color = LuckyGreen.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Divider(
            color = GoldAccent.copy(alpha = 0.4f),
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(bottom = 12.dp)
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            val availableWidth = this.maxWidth
            val numberOfOrbs = 3
            val minGap = 8.dp
            val totalGapSpace = minGap * (numberOfOrbs -1)

            val calculatedOrbSize = ((availableWidth - totalGapSpace) / numberOfOrbs)
                .coerceIn(60.dp, 85.dp)

            val numberFontSize = when {
                calculatedOrbSize >= 80.dp -> 30.sp
                calculatedOrbSize >= 70.dp -> 28.sp
                else -> 24.sp
            }

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                displayedSlotNumbers.forEachIndexed { index, numberString ->
                    val alpha by animateFloatAsState(
                        targetValue = if (isAnimating && displayedSlotNumbers[index].all { it.isDigit() }) 1f else if (displayedSlotNumbers[index] == "?") 0.7f else 1f,
                        animationSpec = tween(durationMillis = if (isAnimating) SPIN_INTERVAL_MS.toInt() else 300),
                        label = "alphaAnim_num_$index"
                    )
                    NumberDisplay(
                        number = numberString,
                        modifier = Modifier.alpha(alpha),
                        orbSize = calculatedOrbSize,
                        fontSize = numberFontSize
                    )
                }
            }
        }

        Divider(
            color = GoldAccent.copy(alpha = 0.4f),
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(top = 12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))
        //delay para mostrar anuncio
        if (numbersGeneratedState.value) {
            LaunchedEffect(Unit) {
                delay(3000)
                showInterstitialTrigger = true
            }
        }

        Button(
            onClick = {
                if (!isAnimating) {
                    userProfile?.let { currentProfile ->
                        numbersViewModel.generateNumbersForScreen("screen1", 3, currentProfile)
                        numbersGeneratedState.value = true
                    }
                }
            },
            enabled = canGenerate && userProfile != null && !isAnimating,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(60.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LuckyGreen.darken(0.1f),
                contentColor = Color.White,
                disabledContainerColor = DisabledButtonColor.copy(alpha = 0.5f)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                text = when {
                    isAnimating -> stringResource(R.string.button_state_revealing)
                    canGenerate -> stringResource(R.string.button_state_generate)
                    else -> stringResource(R.string.button_state_come_back)
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        // --- AQUÍ INSERTAS EL ANUNCIO NATIVO ---
        // Añade un poco de espacio y luego el Composable del anuncio nativo.
        Spacer(modifier = Modifier.height(24.dp))
        AdvancedNativeAdView()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun NumberDisplay( // Este Composable no necesita cambios para la vibración
    number: String,
    modifier: Modifier = Modifier,
    orbSize: Dp = 80.dp,
    fontSize: TextUnit = 30.sp
) {
    val density = LocalDensity.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(orbSize)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GoldAccent.copy(alpha = 0.7f),
                        GoldAccent.copy(alpha = 0.9f),
                        GoldAccent.darken(0.1f)
                    ),
                    radius = with(density) { (orbSize / 2).toPx() } * 2.2f
                ),
                shape = CircleShape
            )
            .border(
                BorderStroke(
                    width = if (orbSize < 70.dp) 1.5.dp else 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            LuckyGreen.copy(alpha = 0.4f),
                            LuckyGreen.darken(0.2f).copy(alpha = 0.7f)
                        )
                    )
                ),
                CircleShape
            )
    ) {
        Text(
            text = number,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = LuckyGreen.darken(0.2f),
            fontFamily = FontFamily.SansSerif
        )
    }
}