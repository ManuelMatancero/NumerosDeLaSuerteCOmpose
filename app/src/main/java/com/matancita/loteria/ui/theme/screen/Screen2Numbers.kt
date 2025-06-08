package com.matancita.loteria.ui.theme.screen

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
import androidx.compose.ui.platform.LocalHapticFeedback // IMPORTADO
import androidx.compose.ui.hapticfeedback.HapticFeedbackType // IMPORTADO
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
import com.matancita.loteria.anuncios.TEST_ADAPTIVE_BANNER_AD_UNIT_ID
import com.matancita.loteria.ui.theme.DisabledButtonColor
import com.matancita.loteria.ui.theme.GoldAccent
import com.matancita.loteria.ui.theme.LightGreen
import com.matancita.loteria.ui.theme.LuckyGreen
import com.matancita.loteria.viewmodel.NumbersViewModel
import com.matancita.loteria.viewmodel.UserDataViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin // IMPORTADO
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

// Constantes de animación para Screen2
private const val SPIN_DURATION_BASE_MS_S2 = 1100L
private const val SPIN_DURATION_STAGGER_MS_S2 = 180L
private const val SPIN_INTERVAL_MS_S2 = 50L
private const val SETTLE_STAGGER_LAUNCH_DELAY_MS_S2 = 250L
private const val VIBRATION_INTERVAL_MS_S2 = 200L // Intervalo para la vibración en esta pantalla

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen2Numbers(
    numbersViewModel: NumbersViewModel,
    userDataViewModel: UserDataViewModel
) {
    val numbersDataState by numbersViewModel.screen2NumbersData.collectAsState()
    val canGenerate by numbersViewModel.canGenerateScreen2.collectAsState()
    val userProfile by userDataViewModel.userProfile.collectAsState()

    val displayedSlotNumbers = remember { mutableStateListOf("?", "?", "?", "?", "?") }
    var isAnimating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current // Obtener la instancia para feedback háptico

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

    // LaunchedEffect para la animación de números y VIBRACIÓN
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
                var vibrationJob: Job? = null // Job para la corrutina de vibración

                try {
                    // --- INICIO DE LA VIBRACIÓN INTERMITENTE ---
                    vibrationJob = launch {
                        while (true) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) // Vibración ligera
                            delay(VIBRATION_INTERVAL_MS_S2) // Usar constante específica para Screen2
                        }
                    }
                    // --- FIN DE INICIO DE VIBRACIÓN ---

                    for (i in 0 until displayedSlotNumbers.size) {
                        if (i >= actualNewNumbers.size) break
                        val job = launch {
                            val spinDuration = SPIN_DURATION_BASE_MS_S2 + (i * SPIN_DURATION_STAGGER_MS_S2)
                            val spinEndTime = System.currentTimeMillis() + spinDuration
                            while (System.currentTimeMillis() < spinEndTime) {
                                displayedSlotNumbers[i] = Random.nextInt(1, 101).toString().padStart(2, '0')
                                delay(SPIN_INTERVAL_MS_S2)
                            }
                            displayedSlotNumbers[i] = actualNewNumbers[i].toString().padStart(2, '0')
                        }
                        animationJobs.add(job)
                        if (i < displayedSlotNumbers.size - 1) {
                            delay(SETTLE_STAGGER_LAUNCH_DELAY_MS_S2)
                        }
                    }

                    animationJobs.joinAll() // Esperar a que terminen las animaciones de los números

                } finally {
                    // --- DETENER LA VIBRACIÓN ---
                    vibrationJob?.cancelAndJoin() // Cancelar y esperar que la corrutina de vibración termine
                    isAnimating = false // Marcar el fin de la animación general
                    // --- FIN DE DETENER VIBRACIÓN ---
                }
            }
        } else if (currentNumbersData == null && canGenerate) {
            previousNumbersDataTimestamp = 0L
        }
    }

    // --- El resto del Composable Column con el UI (sin cambios respecto a la versión anterior) ---
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
        val baseTitle =  stringResource(R.string.magic_numbers_title_user)
        val titleText = if (profile != null && profile.name.isNotBlank()) {
            val nameParts = profile.name.split(" ")
            val displayName = if (nameParts.isNotEmpty() && nameParts[0].length <= 12) {
                nameParts[0]
            } else {
                profile.name.take(12) + if (profile.name.length > 12) "..." else ""
            }
            "$displayName, $baseTitle"
        } else {
            baseTitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
        val titleFontSize = if (titleText.length > 28) 26.sp else 30.sp
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
            text = stringResource(R.string.cosmos_subtitle),
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
                .fillMaxWidth(0.7f)
                .padding(bottom = 12.dp)
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            val availableWidth = this.maxWidth
            val numberOfOrbs = 5
            val minGap = 4.dp
            val totalGapSpace = minGap * (numberOfOrbs - 1)

            val calculatedOrbSize = ((availableWidth - totalGapSpace) / numberOfOrbs)
                .coerceIn(48.dp, 65.dp)

            val numberFontSize = when {
                calculatedOrbSize >= 60.dp -> 24.sp
                calculatedOrbSize >= 50.dp -> 22.sp
                else -> 20.sp
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                displayedSlotNumbers.forEachIndexed { index, numberString ->
                    val alpha by animateFloatAsState(
                        targetValue = if (isAnimating && displayedSlotNumbers[index].all { it.isDigit() }) 1f else if (displayedSlotNumbers[index] == "?") 0.7f else 1f,
                        animationSpec = tween(durationMillis = if (isAnimating) SPIN_INTERVAL_MS_S2.toInt() else 300),
                        label = "alphaAnim_S2_$index"
                    )
                    SmallNumberDisplay(
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
                .fillMaxWidth(0.7f)
                .padding(top = 12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (!isAnimating) {
                    userProfile?.let { currentProfile ->
                        numbersViewModel.generateNumbersForScreen("screen2", 5, currentProfile)
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
                    isAnimating -> stringResource(R.string.button_state_revealing)// Texto actualizado
                    canGenerate -> stringResource(R.string.button_state_reveal)
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

// --- Composable SmallNumberDisplay (sin cambios respecto a la versión anterior) ---
@Composable
fun SmallNumberDisplay(
    number: String,
    modifier: Modifier = Modifier,
    orbSize: Dp = 60.dp,
    fontSize: TextUnit = 24.sp
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
                    width = if (orbSize < 55.dp) 1.dp else 1.5.dp,
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

// --- Color.darken() y otras definiciones (si las tienes en este archivo) ---