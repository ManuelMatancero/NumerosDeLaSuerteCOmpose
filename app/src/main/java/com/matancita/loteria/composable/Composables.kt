package com.matancita.loteria.composable



import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matancita.loteria.ui.theme.GoldAccent
import com.matancita.loteria.ui.theme.LightGreen
import com.matancita.loteria.ui.theme.LuckyGreen
import com.matancita.loteria.ui.theme.screen.darken

@Composable
fun ZodiacSelectionDialog(
    onSignSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val zodiacSigns = listOf(
        "Aries", "Tauro", "Géminis", "Cáncer", "Leo", "Virgo",
        "Libra", "Escorpio", "Sagitario", "Capricornio", "Acuario", "Piscis"
    )

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Elige tu Signo Astral",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = GoldAccent,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(zodiacSigns) { sign ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LuckyGreen.copy(alpha = 0.1f))
                            .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { onSignSelected(sign) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sign,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = { /* No necesitamos botones de confirmar/cancelar */ },
        containerColor = LuckyGreen.darken(0.4f).copy(alpha = 0.95f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.border(1.dp, GoldAccent, RoundedCornerShape(20.dp))
    )
}