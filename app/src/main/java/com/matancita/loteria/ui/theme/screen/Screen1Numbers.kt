package com.matancita.loteria.ui.theme.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.AutoAwesome
import android.util.Log
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringArrayResource
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
import com.matancita.loteria.anuncios.RewardedAdManager
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

    val vipNumbers by numbersViewModel.vipNumbers.collectAsState()
    var isVipDecrypting by remember { mutableStateOf(false) }
    var lastRevealedVip by remember { mutableStateOf<Int?>(null) }

    val context = LocalContext.current
    val activity = LocalActivity.current

    LaunchedEffect(Unit) {
        RewardedAdManager.loadAd(context)
    }

//    LaunchedEffect(Unit) {
//        InterstitialAdManager.loadAd(context, TEST_INTERSTITIAL_AD_UNIT_ID)
//    }

//    if (showInterstitialTrigger) {
//        LaunchedEffect(Unit) {
//            activity?.let {
//                InterstitialAdManager.showAd(it) { /* Ad closed callback */ }
//            }
//            showInterstitialTrigger = false
//        }
//    }

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
                        displayedNumbers[i] = "..."
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

    // --- CORRECCIÓN AQUÍ: Se añade el fondo oscuro ---
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
        // Ahora las estrellas blancas SÍ se verán sobre el fondo oscuro
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

            HorizontalDivider(
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

            HorizontalDivider(
                color = GoldAccent.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth(0.6f).padding(top = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            var numbersGenerated by remember { mutableStateOf(false) }
            if (numbersGenerated) {
                LaunchedEffect(Unit) {
                    delay(3000)
//                    showInterstitialTrigger = true
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
                    disabledContainerColor = Color(0xFF2D2D2D).copy(alpha = 0.8f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
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

            // --- VIP Section ---
            if (!canGenerate && !isAnimating) {
                Spacer(modifier = Modifier.height(24.dp))

                val isRewardedReady by RewardedAdManager.isAdLoadedFlow.collectAsState()

                if (isVipDecrypting) {
                    VipDecryptingView()
                } else {
                    VipVaultCard(
                        hasVips = vipNumbers.isNotEmpty(),
                        isAdReady = isRewardedReady,
                        onUnlock = {
                            if (!isRewardedReady) return@VipVaultCard
                            activity?.let {
                                RewardedAdManager.showAd(
                                    it,
                                    onReward = { _ ->
                                        userProfile?.let { profile ->
                                            val newNumber = numbersViewModel.generateVipNumber(profile)
                                            if (newNumber != -1) {
                                                isVipDecrypting = true
                                                coroutineScope.launch {
                                                    delay(2500)
                                                    numbersViewModel.commitVipNumber(newNumber)
                                                    lastRevealedVip = newNumber
                                                    isVipDecrypting = false
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    delay(5000)
                                                    lastRevealedVip = null
                                                }
                                            }
                                        }
                                    },
                                    onDismiss = {
                                        Log.d("Screen1Numbers", "Rewarded ad dismissed without reward.")
                                    }
                                )
                            }
                        }
                    )
                }

                if (vipNumbers.isNotEmpty() && !isVipDecrypting) {
                    Spacer(modifier = Modifier.height(24.dp))
                    VipCollectionView(
                        numbers = vipNumbers,
                        userName = userProfile?.name?.takeIf { it.isNotBlank() } ?: "",
                        lastRevealed = lastRevealedVip
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))
            if(SHOW_AD){
                AdmobAdaptiveBanner(adUnitId = "ca-app-pub-9861862421891852/2370788758")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// --- Asegúrate de tener estas definiciones al final del archivo si no las has importado ---



data class StarParticle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val phaseOffset: Float
)

@Composable
fun StarlightOrb(
    number: String,
    isAnimating: Boolean,
    modifier: Modifier = Modifier,
    orbSize: Dp = 85.dp,
    fontSize: TextUnit = 30.sp
) {
    val density = LocalDensity.current

    // Animación de opacidad general
    val alpha by animateFloatAsState(
        targetValue = if (number == "?") 0.6f else 1f,
        label = "orbAlpha"
    )

    // Progreso del polvo estelar (stardust)
    val stardustProgress = remember { Animatable(0f) }

    // Generar estrellas una sola vez
    val stars = remember {
        List(12) {
            StarParticle(
                x = (Random.nextFloat() - 0.5f) * 2f,
                y = (Random.nextFloat() - 0.5f) * 2f,
                radius = Random.nextFloat() * 1.6f + 0.8f,
                phaseOffset = Random.nextFloat()
            )
        }
    }

    // Animación infinita para el parpadeo de las estrellas
    val infiniteTransition = rememberInfiniteTransition(label = "star_twinkle_transition")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star_twinkle_progress"
    )

    // Control de la animación del polvo estelar
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            stardustProgress.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            stardustProgress.snapTo(0f)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(orbSize)
            .alpha(alpha) // Modificador alpha de Compose UI
            .drawWithCache {
                val orbBrush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE1BEE7).copy(alpha = 0.4f),
                        Color(0xFF9C27B0).copy(alpha = 0.5f),
                        Color(0xFF4A148C).copy(alpha = 0.6f)
                    ),
                    radius = size.minDimension / 2 * 1.5f
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

                    if (isAnimating) {
                        // Dibujado del polvo estelar giratorio
                        val center = center // center del DrawScope
                        val maxRadius = size.width / 2.5f
                        val currentProgress = stardustProgress.value

                        // Creamos 3 partículas de polvo orbitando
                        for(i in 0..2) {
                            val offsetAngle = i * 120f
                            val angle = (currentProgress * 360f * 2) + offsetAngle // * 2 para más velocidad
                            val radius = currentProgress * maxRadius

                            val x = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * radius
                            val y = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * radius

                            val particleAlpha = (1 - currentProgress).coerceIn(0f, 1f)
                            drawCircle(
                                color = Color.White.copy(alpha = particleAlpha),
                                radius = 2.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
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

            // Canvas para las estrellas internas
            Canvas(modifier = Modifier.matchParentSize()) {
                val centerX = size.width / 2
                val centerY = size.height / 2

                stars.forEach { star ->
                    val wave = sin((animationProgress + star.phaseOffset) * 2 * Math.PI).toFloat()
                    val starAlpha = (wave * 0.5f + 0.5f).coerceIn(0.1f, 1f)

                    drawCircle(
                        color = GoldAccent,
                        radius = star.radius,
                        alpha = starAlpha,
                        center = Offset(
                            x = centerX + (star.x * centerX * 0.7f),
                            y = centerY + (star.y * centerY * 0.7f)
                        )
                    )
                }
            }
        }
    }
}



@Composable
private fun VipVaultCard(
    hasVips: Boolean,
    isAdReady: Boolean,
    onUnlock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(0.9f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1025)),
        border = BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                listOf(
                    GoldAccent.copy(alpha = 0.3f),
                    GoldAccent.copy(alpha = 0.8f),
                    GoldAccent.copy(alpha = 0.3f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(GoldAccent.copy(alpha = 0.25f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasVips) Icons.Rounded.AutoAwesome else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (hasVips) stringResource(R.string.vip_vault_title_has_vips) else stringResource(R.string.vip_vault_title_no_vips),
                color = GoldAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasVips)
                    stringResource(R.string.vip_vault_desc_has_vips)
                else
                    stringResource(R.string.vip_vault_desc_no_vips),
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onUnlock,
                enabled = isAdReady,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA000),
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFFFFA000).copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(
                    imageVector = if (hasVips) Icons.Rounded.AutoAwesome else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = if (isAdReady) 1f else 0.5f)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    text = when {
                        !isAdReady -> stringResource(R.string.vip_button_loading)
                        hasVips -> stringResource(R.string.vip_button_claim_another)
                        else -> stringResource(R.string.vip_button_unlock)
                    },
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black.copy(alpha = if (isAdReady) 1f else 0.5f)
                )
            }
        }
    }
}

@Composable
private fun VipDecryptingView() {
    val infiniteTransition = rememberInfiniteTransition(label = "decrypt_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val rotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    Card(
        modifier = Modifier.fillMaxWidth(0.9f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1025)),
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.vip_decrypting_title),
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 3.dp.toPx())
                    drawArc(
                        color = GoldAccent.copy(alpha = 0.7f),
                        startAngle = rotate,
                        sweepAngle = 100f,
                        useCenter = false,
                        style = stroke
                    )
                    drawArc(
                        color = GoldAccent.copy(alpha = 0.3f),
                        startAngle = rotate + 180f,
                        sweepAngle = 60f,
                        useCenter = false,
                        style = stroke
                    )
                }
                Box(
                    modifier = Modifier
                        .size(80.dp * pulse)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(GoldAccent.copy(alpha = 0.3f), Color.Transparent)
                            ),
                            shape = RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "??",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                color = GoldAccent,
                trackColor = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth(0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.vip_decrypting_subtitle),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun VipCollectionView(
    numbers: List<Int>,
    userName: String,
    lastRevealed: Int?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.vip_collection_title),
            color = GoldAccent,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            numbers.forEach { num ->
                val isNew = num == lastRevealed
                VipNumberChip(number = num, isNew = isNew, userName = userName)
            }
        }
    }
}

@Composable
private fun VipNumberChip(
    number: Int,
    isNew: Boolean,
    userName: String
) {
    val scale by animateFloatAsState(
        targetValue = if (isNew) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 200f),
        label = "vipScale"
    )
    val (rarityRes, rarityColor) = getVipRarity(number)
    val rarityName = stringResource(rarityRes)
    val fortunes = stringArrayResource(R.array.vip_fortunes).toList()
    val fortune = remember(number, userName) { getVipFortune(number, userName, fortunes) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(if (isNew) 100.dp else 70.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            rarityColor.copy(alpha = 0.25f),
                            rarityColor.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(50)
                )
                .border(
                    width = if (isNew) 2.5.dp else 1.5.dp,
                    brush = Brush.linearGradient(listOf(rarityColor, GoldAccent)),
                    shape = RoundedCornerShape(50)
                )
        ) {
            Text(
                text = number.toString().padStart(2, '0'),
                fontSize = if (isNew) 28.sp else 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isNew) Color.White else Color.White.copy(alpha = 0.9f)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            color = rarityColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.5.dp, rarityColor.copy(alpha = 0.5f))
        ) {
            Text(
                text = rarityName.uppercase(),
                color = rarityColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                letterSpacing = 0.5.sp
            )
        }
        if (isNew) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "\"$fortune\"",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.85f),
                lineHeight = 16.sp
            )
        }
    }
}

private fun getVipRarity(number: Int): Pair<Int, Color> {
    return when {
        number == 100 -> R.string.vip_rarity_cosmic_legend to Color(0xFFFFD700)
        number >= 76 -> R.string.vip_rarity_solar_flare to Color(0xFFFFA500)
        number >= 51 -> R.string.vip_rarity_moonbeam to Color(0xFF9C27B0)
        number >= 26 -> R.string.vip_rarity_starlight to Color(0xFF03A9F4)
        else -> R.string.vip_rarity_nebula_core to Color(0xFF4CAF50)
    }
}

private fun getVipFortune(number: Int, userName: String, fortunes: List<String>): String {
    val personalization = userName.length.coerceAtLeast(1)
    val index = (number * 7 + personalization * 3) % fortunes.size
    return fortunes[index]
}
