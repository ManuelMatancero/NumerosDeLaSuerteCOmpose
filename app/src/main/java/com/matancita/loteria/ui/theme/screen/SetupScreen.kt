package com.matancita.loteria.ui.theme.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matancita.loteria.R
import com.matancita.loteria.ui.theme.GoldAccent
import com.matancita.loteria.ui.theme.LightGreen
import com.matancita.loteria.ui.theme.LuckyGreen
import com.matancita.loteria.viewmodel.UserDataViewModel
import java.text.SimpleDateFormat
import java.util.*

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // Añadir ExperimentalLayoutApi para imePadding
@Composable
fun SetupScreen(
    userDataViewModel: UserDataViewModel,
    onSetupComplete: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dobTimestamp by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    dobTimestamp?.let { calendar.timeInMillis = it }

    val dateFormatter = SimpleDateFormat("dd / MM / yy", Locale.getDefault())
    val selectedDateText = dobTimestamp?.let { dateFormatter.format(Date(it)) } ?: "Fecha de Nacimiento"

    val openDialog = remember { mutableStateOf(false) }

    if (openDialog.value) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                dobTimestamp = calendar.timeInMillis
                openDialog.value = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
            show()
        }
    }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp

    val imageSize: Dp = (screenWidthDp * 0.25f).coerceIn(80.dp, 120.dp)
    val titleFontSize: TextUnit = if (screenWidthDp < 360.dp) 26.sp else 30.sp
    val subtitleFontSize: TextUnit = if (screenWidthDp < 360.dp) 14.sp else 16.sp

    Box(
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
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 550.dp)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(), // <--- MEJORA CLAVE AQUÍ: Añade padding para el teclado
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height((screenHeightDp * 0.02f).coerceAtLeast(16.dp)))

            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo de la App",
                modifier = Modifier
                    .size(imageSize)
                    .clip(RoundedCornerShape((imageSize.value * 0.2f).dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height((screenHeightDp * 0.03f).coerceIn(16.dp, 28.dp)))

            Text(
                "¡Bienvenido al Oráculo!",
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = LuckyGreen.darken(0.1f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = (screenHeightDp * 0.015f).coerceIn(8.dp, 12.dp))
            )
            Text(
                "Ingresa tus datos para una experiencia mística.",
                fontSize = subtitleFontSize,
                fontFamily = FontFamily.SansSerif,
                color = LuckyGreen.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = (screenHeightDp * 0.035f).coerceIn(24.dp, 36.dp))
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tu Nombre Completo", color = LuckyGreen.copy(alpha = 0.7f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                textStyle = TextStyle(color = LuckyGreen.darken(0.1f), fontSize = 16.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LuckyGreen.darken(0.2f),
                    unfocusedTextColor = LuckyGreen.darken(0.1f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    cursorColor = GoldAccent,
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = LuckyGreen.copy(alpha = 0.4f),
                    focusedLabelColor = GoldAccent,
                    unfocusedLabelColor = LuckyGreen.copy(alpha = 0.7f)
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height((screenHeightDp * 0.02f).coerceIn(16.dp, 20.dp)))

            OutlinedButton(
                onClick = { openDialog.value = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, if (dobTimestamp != null) GoldAccent else LuckyGreen.copy(alpha = 0.4f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        selectedDateText,
                        color = if (dobTimestamp != null) LuckyGreen.darken(0.1f) else LuckyGreen.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = "Seleccionar Fecha",
                        tint = if (dobTimestamp != null) GoldAccent else LuckyGreen.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height((screenHeightDp * 0.04f).coerceIn(24.dp, 40.dp)))

            Button(
                onClick = {
                    if (name.isNotBlank() && dobTimestamp != null) {
                        userDataViewModel.saveUserProfile(name, dobTimestamp!!)
                        onSetupComplete()
                    }
                },
                enabled = name.isNotBlank() && dobTimestamp != null,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LuckyGreen.darken(0.1f),
                    contentColor = Color.White,
                    disabledContainerColor = LuckyGreen.copy(alpha = 0.3f, red = 0.6f, green = 0.6f, blue = 0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 3.dp)
            ) {
                Text("GUARDAR Y CONTINUAR", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height((screenHeightDp * 0.02f).coerceAtLeast(16.dp)))
        }
    }
}