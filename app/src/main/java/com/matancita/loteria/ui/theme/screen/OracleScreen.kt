package com.matancita.loteria.ui.theme.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matancita.loteria.R
import com.matancita.loteria.anuncios.AdmobAdaptiveBanner
import com.matancita.loteria.viewmodel.OracleGameState
import com.matancita.loteria.viewmodel.OracleViewModel

@Composable
fun OracleOfTimeScreen(viewModel: OracleViewModel = viewModel()) {
    val gameState by viewModel.gameState.collectAsState()
    val oracleNumbers by viewModel.oracleNumbers.collectAsState()
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize()) {
        StarryNightBackground()
        // BoxWithConstraints nos da las dimensiones del espacio disponible para hacer el UI responsivo.
        BoxWithConstraints {
            val screenHeight = maxHeight
            val screenWidth = maxWidth
            val isLandscape = screenWidth > screenHeight

            // Elige el layout basado en la orientación
            if (isLandscape) {
                LandscapeLayout(
                    gameState = gameState,
                    oracleNumbers = oracleNumbers ?: emptyList(),
                    viewModel = viewModel,
                    haptic = haptic
                )
            } else {
                PortraitLayout(
                    gameState = gameState,
                    oracleNumbers = oracleNumbers ?: emptyList(),
                    viewModel = viewModel,
                    haptic = haptic
                )
            }
        }
    }
}

@Composable
private fun PortraitLayout(
    gameState: OracleGameState,
    oracleNumbers: List<Int>,
    viewModel: OracleViewModel,
    haptic: HapticFeedback // CORRECCIÓN: El tipo es HapticFeedback
) {
    BoxWithConstraints {
        val screenHeight = maxHeight
        val screenWidth = maxWidth

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            OracleTitle(screenHeight)
            val clockSize = (minOf(screenWidth, screenHeight) * 0.7f).coerceAtMost(400.dp)
            OracleClock(
                modifier = Modifier.size(clockSize),
                gameState = gameState,
                numbers = oracleNumbers,
                onStop = {
                    if (gameState == OracleGameState.READY_TO_PLAY) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.stopOracle()
                    }
                }
            )
            OracleControls(
                gameState = gameState,
                numbers = oracleNumbers,
                buttonSize = (clockSize * 0.25f).coerceIn(60.dp, 80.dp),
                numberCircleSize = (clockSize * 0.15f).coerceIn(40.dp, 55.dp)
            ) {
                if (gameState == OracleGameState.READY_TO_PLAY) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.stopOracle()
                }
            }

            AdmobAdaptiveBanner(adUnitId="ca-app-pub-9861862421891852/2370788758")
        }
    }
}

@Composable
private fun LandscapeLayout(
    gameState: OracleGameState,
    oracleNumbers: List<Int>,
    viewModel: OracleViewModel,
    haptic: HapticFeedback // CORRECCIÓN: El tipo es HapticFeedback
) {
    BoxWithConstraints {
        val screenHeight = maxHeight
        val screenWidth = maxWidth

        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OracleTitle(screenWidth, modifier = Modifier.weight(1f))

            val clockSize = (screenHeight * 0.8f).coerceAtMost(350.dp)
            OracleClock(
                modifier = Modifier.size(clockSize).weight(1.5f),
                gameState = gameState,
                numbers = oracleNumbers,
                onStop = {
                    if (gameState == OracleGameState.READY_TO_PLAY) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.stopOracle()
                    }
                }
            )

            OracleControls(
                modifier = Modifier.weight(1f),
                gameState = gameState,
                numbers = oracleNumbers,
                buttonSize = (clockSize * 0.25f).coerceIn(60.dp, 80.dp),
                numberCircleSize = (clockSize * 0.15f).coerceIn(40.dp, 55.dp)
            ) {
                if (gameState == OracleGameState.READY_TO_PLAY) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.stopOracle()
                }
            }
        }
    }
}


@Composable
private fun OracleTitle(sizeReference: Dp, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.oracle_title),
            fontSize = (sizeReference.value * 0.04f).coerceAtLeast(26.0f).sp,
            fontFamily = FontFamily.Serif,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(id = R.string.oracle_subtitle),
            fontSize = (sizeReference.value * 0.02f).coerceAtLeast(14.0f).sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}


@Composable
private fun OracleClock(
    modifier: Modifier = Modifier,
    gameState: OracleGameState,
    numbers: List<Int>,
    onStop: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "oracle_rotation")

    val outerRingRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(30000, easing = LinearEasing)), label = "outer_ring"
    )
    val innerRingRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)), label = "inner_ring"
    )

    val handsRunning = gameState == OracleGameState.READY_TO_PLAY
    val hand1Angle by animateFloatAsState(
        targetValue = if (handsRunning) 360f else (numbers.getOrNull(0) ?: 0) * 3.6f,
        animationSpec = if (handsRunning) tween(1000) else spring(dampingRatio = 0.4f, stiffness = 50f), label = "hand1_stop"
    )
    val hand2Angle by animateFloatAsState(
        targetValue = if (handsRunning) 360f else (numbers.getOrNull(1) ?: 0) * 3.6f,
        animationSpec = if (handsRunning) tween(800) else spring(dampingRatio = 0.4f, stiffness = 50f), label = "hand2_stop"
    )
    val hand3Angle by animateFloatAsState(
        targetValue = if (handsRunning) 360f else (numbers.getOrNull(2) ?: 0) * 3.6f,
        animationSpec = if (handsRunning) tween(1200) else spring(dampingRatio = 0.4f, stiffness = 50f), label = "hand3_stop"
    )

    val hand1ContinuousRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart), label = "hand1_continuous"
    )
    val hand2ContinuousRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart), label = "hand2_continuous"
    )
    val hand3ContinuousRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -360f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart), label = "hand3_continuous"
    )

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onStop
            )
    ) {
        val radius = size.minDimension / 2
        val center = this.center

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color(0xFF89CFF0).copy(alpha = 0.5f), Color.Transparent),
                center = center,
                radius = radius * 0.3f
            ),
            radius = radius * 0.3f,
            center = center
        )

        rotate(outerRingRotation, center) {
            drawCircle(color = Color.White.copy(alpha = 0.2f), radius = radius, style = Stroke(1.dp.toPx()))
        }
        rotate(innerRingRotation, center) {
            drawCircle(color = Color.White.copy(alpha = 0.2f), radius = radius * 0.7f, style = Stroke(1.dp.toPx()))
        }

        if (handsRunning) {
            rotate(hand1ContinuousRotation, center) { drawLine(Color(0xFF89CFF0), center, Offset(center.x, center.y - radius * 0.9f), 2.dp.toPx()) }
            rotate(hand2ContinuousRotation, center) { drawLine(Color(0xFF89CFF0), center, Offset(center.x, center.y - radius * 0.8f), 2.dp.toPx()) }
            rotate(hand3ContinuousRotation, center) { drawLine(Color(0xFF89CFF0), center, Offset(center.x, center.y - radius * 0.6f), 2.dp.toPx()) }
        } else {
            rotate(hand1Angle, center) { drawLine(Color(0xFFFFD700), center, Offset(center.x, center.y - radius * 0.9f), 3.dp.toPx()) }
            rotate(hand2Angle, center) { drawLine(Color(0xFFFFD700), center, Offset(center.x, center.y - radius * 0.8f), 3.dp.toPx()) }
            rotate(hand3Angle, center) { drawLine(Color(0xFFFFD700), center, Offset(center.x, center.y - radius * 0.6f), 3.dp.toPx()) }
        }
    }
}

@Composable
private fun OracleControls(
    modifier: Modifier = Modifier,
    gameState: OracleGameState,
    numbers: List<Int>,
    buttonSize: Dp,
    numberCircleSize: Dp,
    onStop: () -> Unit = {}
) {
    Column(
        modifier = modifier.heightIn(min = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (gameState == OracleGameState.REVEALED) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    numbers.forEach { number ->
                        NumberCircle(number = number.toString(), isSpecial = true, size = numberCircleSize)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(id = R.string.oracle_come_back_tomorrow), color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center)
            }
        } else {
            Button(
                onClick = onStop,
                shape = CircleShape,
                modifier = Modifier.size(buttonSize),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.3f))
            ) {
                if (gameState == OracleGameState.READY_TO_PLAY) {
                    Icon(
                        Icons.Default.TouchApp,
                        contentDescription = stringResource(id = R.string.oracle_stop_button),
                        tint = Color.White,
                        modifier = Modifier.size(buttonSize * 0.4f)
                    )
                } else {
                    CircularProgressIndicator(modifier=Modifier.size(buttonSize * 0.4f), color=Color.White, strokeWidth = 3.dp)
                }
            }
        }
    }
}

@Composable
private fun NumberCircle(number: String, isSpecial: Boolean, size: Dp) {
    val circleColor = if (isSpecial) Color(0xFFFFD700) else Color.White
    Box(
        modifier = Modifier
            .size(size)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(circleColor.copy(alpha = 0.3f), Color.Transparent)
                ),
                shape = CircleShape
            )
            .border(1.dp, circleColor.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            color = circleColor,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.4f).sp,
            textAlign = TextAlign.Center,
            style = TextStyle(shadow = Shadow(circleColor, blurRadius = 10f))
        )
    }
}
