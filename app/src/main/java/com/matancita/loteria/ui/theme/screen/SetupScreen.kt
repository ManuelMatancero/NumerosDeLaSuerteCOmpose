package com.matancita.loteria.ui.theme.screen

import android.app.DatePickerDialog
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matancita.loteria.R
import com.matancita.loteria.anuncios.AdvancedNativeAdView
import com.matancita.loteria.ui.theme.DisabledButtonColor
import com.matancita.loteria.ui.theme.GoldAccent
import com.matancita.loteria.viewmodel.HoroscopeViewModel
import com.matancita.loteria.viewmodel.UserDataViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


// --- Pantalla de Configuración Rediseñada ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    val dateFormatter = SimpleDateFormat("dd / MM / yyyy", Locale.getDefault())
    val selectedDateText = dobTimestamp?.let { dateFormatter.format(Date(it)) } ?: stringResource(id = R.string.setup_dob_placeholder)

    val openDialog = remember { mutableStateOf(false) }

    if (openDialog.value) {
        val datePickerDialog = DatePickerDialog(
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
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
        // Recordar descartar el diálogo si el usuario cancela
        datePickerDialog.setOnDismissListener { openDialog.value = false }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        StarryNightBackground()

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 550.dp)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.suertelogo),
                contentDescription = stringResource(id = R.string.setup_app_logo_cd),
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(2.dp, Color.White.copy(alpha = 0.3f)), CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.setup_welcome_title),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = stringResource(id = R.string.setup_welcome_subtitle),
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(id = R.string.setup_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                textStyle = TextStyle(color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.7f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = Color.White.copy(alpha = 0.8f),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = { openDialog.value = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, if (dobTimestamp != null) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.3f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        selectedDateText,
                        color = if (dobTimestamp != null) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.6f),
                        fontSize = 16.sp
                    )
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = stringResource(id = R.string.setup_select_date_cd),
                        tint = if (dobTimestamp != null) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && dobTimestamp != null) {
                        userDataViewModel.saveUserProfile(name.trim(), dobTimestamp!!)
                        onSetupComplete()
                    }
                },
                enabled = name.isNotBlank() && dobTimestamp != null,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(60.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A148C).copy(alpha = 0.7f),
                    contentColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 4.dp)
            ) {
                Text(stringResource(id = R.string.setup_button_save), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
