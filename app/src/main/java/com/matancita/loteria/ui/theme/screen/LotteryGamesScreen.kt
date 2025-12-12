package com.matancita.loteria.ui.theme.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matancita.loteria.R
import com.matancita.loteria.anuncios.AdvancedNativeAdView
import com.matancita.loteria.viewmodel.GameNumbers
import com.matancita.loteria.viewmodel.LotteryGamesViewModel
import com.matancita.loteria.viewmodel.SuperLotoMasData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val SHOW_AD = true

// Colores de Lotería
private val BallWhite = Color(0xFFF5F5F5)
private val BallGold = Color(0xFFFFD700)
private val BallRed = Color(0xFFFF5252)
private val CardBorder = Color(0xFFFFFFFF).copy(alpha = 0.2f)

@Composable
fun LotteryGamesScreen(
    viewModel: LotteryGamesViewModel = viewModel()
) {
    val superLotoMas by viewModel.superLotoMas.collectAsState()
    val superKinoTv by viewModel.superKinoTv.collectAsState()
    val lotoPool by viewModel.lotoPool.collectAsState()
    val pegaTres by viewModel.pegaTres.collectAsState()
    val quinielaPale by viewModel.quinielaPale.collectAsState()
    val loteriaReal by viewModel.loteriaReal.collectAsState()
    val canGenerate by viewModel.canGenerate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val haptic = LocalHapticFeedback.current
    val todayDate = remember {
        SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(Date()).uppercase()
    }

    // Efecto de vibración al terminar de cargar
    LaunchedEffect(isLoading) {
        if (!isLoading && !canGenerate) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo Profundo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0D1117), Color(0xFF1F1B24), Color(0xFF0D1117))
                    )
                )
        )

        StarryNightBackground() // Tu fondo de estrellas

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Elegante
            HeaderSection(todayDate)

            // Lista de Juegos
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    // Super Loto (Featured)
                    MagicLotteryCard(
                        title = stringResource(R.string.Super),
                        subtitle = "Acumulado Millonario",
                        accentColor = BallGold,
                        isLoading = isLoading
                    ) {
                        if (superLotoMas != null) {
                            (superLotoMas as? SuperLotoMasData)?.let { data ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    LotteryBallRow(numbers = data.mainNumbers, delayBase = 0)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        SpecialBall(label = "Súper", number = data.superBall, color = BallGold, delay = 600)
                                        SpecialBall(label = "Más", number = data.superMasBall, color = BallRed, delay = 800)
                                    }
                                }
                            }
                        } else {
                            EmptyStateBalls(7)
                        }
                    }
                }

                item { if (SHOW_AD) AdvancedNativeAdView() }

                item {
                    MagicLotteryCard(title = stringResource(R.string.kino), isLoading = isLoading) {
                        GameRowOrEmpty(superKinoTv)
                    }
                }
                item {
                    MagicLotteryCard(title = stringResource(R.string.pool), isLoading = isLoading) {
                        GameRowOrEmpty(lotoPool)
                    }
                }
                item {
                    MagicLotteryCard(title = stringResource(R.string.pega), isLoading = isLoading) {
                        GameRowOrEmpty(pegaTres)
                    }
                }
                item {
                    MagicLotteryCard(title = stringResource(R.string.Pale), isLoading = isLoading) {
                        GameRowOrEmpty(quinielaPale)
                    }
                }
                item {
                    MagicLotteryCard(title = stringResource(R.string.real), isLoading = isLoading) {
                        GameRowOrEmpty(loteriaReal)
                    }
                }
            }
        }

        // Botón Flotante de Generación (Estilo FAB extendido)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            GenerateButton(
                canGenerate = canGenerate,
                isLoading = isLoading,
                onGenerate = { viewModel.generateAllGames() }
            )
        }
    }
}

// --- Componentes UI ---

@Composable
fun HeaderSection(date: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TU SUERTE DE HOY",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.White,
                shadow = Shadow(color = Color(0xFF9C27B0), blurRadius = 20f)
            )
        )
        Text(
            text = date,
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )
        )
    }
}

@Composable
fun GenerateButton(
    canGenerate: Boolean,
    isLoading: Boolean,
    onGenerate: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "scale"
    )

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onGenerate()
        },
        enabled = canGenerate && !isLoading,
        modifier = Modifier
            .height(56.dp)
            .width(240.dp)
            .scale(if(canGenerate && !isLoading) scale else 1f)
            .shadow(20.dp, RoundedCornerShape(50), spotColor = Color(0xFFD500F9)),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (canGenerate && !isLoading) {
                        Brush.horizontalGradient(listOf(Color(0xFF7B1FA2), Color(0xFFBA68C8)))
                    } else {
                        Brush.horizontalGradient(listOf(Color.Gray.copy(0.5f), Color.Gray.copy(0.5f)))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(targetState = isLoading, label = "btn_content") { loading ->
                if (loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if(canGenerate) Icons.Rounded.AutoAwesome else Icons.Rounded.Casino,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (canGenerate) stringResource(R.string.button_state_generate) else stringResource(R.string.button_state_come_back),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MagicLotteryCard(
    title: String,
    subtitle: String? = null,
    accentColor: Color = Color(0xFFBA68C8),
    isLoading: Boolean,
    content: @Composable () -> Unit
) {
    // Animación de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { 50 }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.White.copy(alpha = 0.03f)
                            )
                        )
                    )
                    .border(BorderStroke(1.dp, Brush.verticalGradient(listOf(CardBorder, Color.Transparent))), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Header de la Tarjeta
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                            if (subtitle != null) {
                                Text(
                                    text = subtitle,
                                    color = accentColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Indicador de estado (Cargando o Listo)
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = accentColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contenido (Bolas)
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        content()
                    }
                }
            }
        }
    }
}

// --- Bolas de Lotería 3D ---

@Composable
fun LotteryBallRow(numbers: List<Int>, delayBase: Int = 0) {
    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 7,
        modifier = Modifier.fillMaxWidth()
    ) {
        numbers.forEachIndexed { index, number ->
            LotteryBall3D(
                number = number.toString(),
                color = BallWhite,
                delay = delayBase + (index * 100) // Cascada
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun SpecialBall(label: String, number: Int, color: Color, delay: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        LotteryBall3D(
            number = number.toString(),
            color = color,
            isSpecial = true,
            delay = delay
        )
    }
}

@Composable
fun LotteryBall3D(
    number: String,
    color: Color,
    isSpecial: Boolean = false,
    delay: Int = 0
) {
    var isVisible by remember { mutableStateOf(false) }

    // Simula el "Rolling" de la bola
    LaunchedEffect(number) {
        isVisible = false
        delay(delay.toLong())
        isVisible = true
    }

    val size = if (isSpecial) 48.dp else 42.dp
    val fontSize = if (isSpecial) 18.sp else 16.sp
    val textColor = if (isSpecial && color != BallWhite) Color.Black else Color.Black

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(animationSpec = spring(dampingRatio = 0.6f)) + fadeIn()
        ) {
            // Renderizado de la Bola 3D
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = this.size.minDimension / 2

                // 1. Base Gradient (Simula esfera)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color, // Centro brillante
                            color.copy(red = color.red * 0.8f, green = color.green * 0.8f, blue = color.blue * 0.8f) // Borde oscuro
                        ),
                        center = center.copy(x = center.x - radius * 0.3f, y = center.y - radius * 0.3f),
                        radius = radius * 2.5f
                    ),
                    radius = radius
                )

                // 2. Brillo especular (Reflejo de luz)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.8f), Color.Transparent),
                        center = center.copy(x = center.x - radius * 0.4f, y = center.y - radius * 0.4f),
                        radius = radius * 0.6f
                    ),
                    radius = radius * 0.5f,
                    center = center.copy(x = center.x - radius * 0.4f, y = center.y - radius * 0.4f)
                )

                // 3. Sombra exterior (Drop shadow)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.2f), Color.Transparent),
                        radius = radius * 1.2f
                    ),
                    radius = radius * 1.2f,
                    center = center.copy(y = center.y + radius * 0.1f)
                )
            }
        }

        // Texto animado (Rolling Effect)
        if (isVisible) {
            Text(
                text = number,
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helpers
@Composable
fun GameRowOrEmpty(data: Any?) {
    if (data is GameNumbers) {
        LotteryBallRow(numbers = data.numbers)
    } else {
        EmptyStateBalls(5)
    }
}

@Composable
fun EmptyStateBalls(count: Int) {
    Row(horizontalArrangement = Arrangement.Center) {
        repeat(count) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .padding(4.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            )
        }
    }
}