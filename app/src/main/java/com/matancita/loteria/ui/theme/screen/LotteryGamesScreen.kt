package com.matancita.loteria.ui.theme.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
import java.text.SimpleDateFormat
import java.util.*

private const val SHOW_AD = true

@OptIn(ExperimentalAnimationApi::class)
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


    val todayDate = remember {
        SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(Date())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        StarryNightBackground() // Reusing the magical background

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = todayDate,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 16.dp),
                fontFamily = FontFamily.Serif
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    LotteryGameCard(
                        title = stringResource(R.string.Super),
                        data = superLotoMas,
                        isLoading = isLoading,
                        numberFormatter = {
                            (it as? SuperLotoMasData)?.let { data ->
                                // Updated to show both special balls
                                NumberRow(
                                    numbers = data.mainNumbers,
                                    specialNumbers = mapOf(
                                        "Súper" to data.superBall,
                                        "Super +" to data.superMasBall
                                    )
                                )
                            }
                        }
                    )
                }
                item {
                    if(SHOW_AD){
                        AdvancedNativeAdView()
                    }
                }
                item {
                    LotteryGameCard(title = stringResource(R.string.kino), data = superKinoTv, isLoading = isLoading)
                }
                item {
                    LotteryGameCard(title = stringResource(R.string.pool), data = lotoPool, isLoading = isLoading)
                }
                item {
                    LotteryGameCard(title = stringResource(R.string.pega), data = pegaTres, isLoading = isLoading)
                }
                item {
                    LotteryGameCard(title = stringResource(R.string.Pale), data = quinielaPale, isLoading = isLoading)
                }
                item {
                    LotteryGameCard(title = stringResource(R.string.real), data = loteriaReal, isLoading = isLoading)
                }
            }
            val genNumbers = stringResource(R.string.button_state_generate)
            val comeBack = stringResource(R.string.button_state_come_back)

            Button(
                onClick = { viewModel.generateAllGames() },
                enabled = canGenerate && !isLoading,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(bottom = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A148C).copy(alpha = 0.7f),
                    contentColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                AnimatedContent(targetState = isLoading, label = "button-text") { loading ->
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = if (canGenerate) genNumbers else comeBack,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LotteryGameCard(
    title: String,
    data: Any?,
    isLoading: Boolean,
    numberFormatter: (@Composable (Any) -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.defaultMinSize(minHeight = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (data != null) {
                    if (numberFormatter != null) {
                        numberFormatter(data)
                    } else if (data is GameNumbers) {
                        NumberRow(numbers = data.numbers)
                    }
                } else {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White.copy(alpha = 0.7f), strokeWidth = 2.dp)
                    } else {
                        Text("?", fontSize = 24.sp, color = Color.White.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NumberRow(
    numbers: List<Int>,
    specialNumbers: Map<String, Int>? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 7
        ) {
            numbers.forEach { number ->
                NumberCircle(number = number.toString(), isSpecial = false)
            }
        }
        specialNumbers?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.width(150.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                it.forEach { (title, number) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(title, color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        NumberCircle(number = number.toString(), isSpecial = true)
                    }
                }
            }
        }
    }
}


@Composable
private fun NumberCircle(number: String, isSpecial: Boolean) {
    val circleColor = if (isSpecial) Color(0xFFFFD700) else Color.White
    Box(
        modifier = Modifier
            .size(40.dp)
            .padding(4.dp)
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
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
