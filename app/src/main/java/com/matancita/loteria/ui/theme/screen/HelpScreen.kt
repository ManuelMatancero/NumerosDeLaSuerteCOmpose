package com.matancita.loteria.ui.theme.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matancita.loteria.R

// Define la estructura de cada sección de ayuda
private data class HelpSection(
    val icon: ImageVector,
    val titleResId: Int,
    val descriptionResId: Int
)

@Composable
fun HelpScreen() {
    // Lista con la información de cada pantalla
    val helpSections = listOf(
        HelpSection(Icons.Default.Looks3, R.string.tab_suerte, R.string.help_desc_lucky_numbers),
        HelpSection(Icons.Default.Looks5, R.string.tab_chance, R.string.help_desc_chance),
        HelpSection(Icons.Default.Casino, R.string.tab_lottery, R.string.help_desc_lottery_games),
        HelpSection(Icons.Default.AutoAwesome, R.string.tab_horoscope, R.string.help_desc_horoscope),
        HelpSection(Icons.Default.Search, R.string.tab_lucky_search, R.string.help_desc_find_fortune),
        HelpSection(Icons.Default.TrackChanges, R.string.tab_oracle, R.string.help_desc_oracle), // NUEVA SECCIÓN
        HelpSection(Icons.Default.Bed, R.string.tab_dreams, R.string.help_desc_dreams)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        StarryNightBackground() // Reutiliza el fondo mágico

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.help_center_title),
                color = Color.White,
                fontSize = 24.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(helpSections) { section ->
                    HelpCard(
                        icon = section.icon,
                        title = stringResource(id = section.titleResId),
                        description = stringResource(id = section.descriptionResId)
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpCard(icon: ImageVector, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        // El layout ahora es una Columna para dar prioridad al ícono.
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF89CFF0),
                modifier = Modifier.size(64.dp) // Ícono más grande y prominente
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center // Texto centrado para fácil lectura
            )
        }
    }
}
