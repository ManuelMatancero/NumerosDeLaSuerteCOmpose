package com.matancita.loteria.ui.theme.screen

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matancita.loteria.R

// --- Definiciones de Color de Ejemplo (reemplaza con las tuyas de Theme.kt si es necesario) ---
val LuckyGreen = Color(0xFF1A5E20)
val LightGreen = Color(0xFF8BC34A)
val GoldAccent = Color(0xFFFFC107)

fun Color.darken(factor: Float): Color {
    val invertedFactor = (1 - factor).coerceIn(0f, 1f)
    return Color(
        red = this.red * invertedFactor,
        green = this.green * invertedFactor,
        blue = this.blue * invertedFactor,
        alpha = this.alpha
    )
}

data class DreamItem(val number: Int, val text: String, val meaning: String)

@Composable
fun rememberDreamList(): List<DreamItem> {
    val dreamNames = stringArrayResource(id = R.array.dream_names)
    val dreamMeanings = stringArrayResource(id = R.array.dream_meanings)

    return remember(dreamNames, dreamMeanings) {
        dreamNames.mapIndexed { index, name ->
            DreamItem(
                number = index + 1,
                text = name,
                meaning = dreamMeanings.getOrElse(index) { "" } // Seguridad por si los arrays no coinciden
            )
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Screen3Dreams() {
    val allDreamExperiences = rememberDreamList() // Cargar la lista desde los recursos
    var searchQuery by remember { mutableStateOf("") }
    var selectedDreamNumber by remember { mutableStateOf<Int?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current

    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

    val desiredOrbSize = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        (configuration.screenWidthDp.dp * 0.33f).coerceIn(100.dp, 150.dp)
    } else {
        (configuration.screenHeightDp.dp * 0.45f).coerceIn(80.dp, 120.dp)
    }

    val filteredDreamItems by remember(searchQuery, allDreamExperiences) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                allDreamExperiences
            } else {
                val queryLower = searchQuery.lowercase()
                allDreamExperiences.filter { dreamItem ->
                    dreamItem.text.lowercase().contains(queryLower) ||
                            dreamItem.number.toString().contains(queryLower)
                }
            }
        }
    }

    val onDreamItemClickLambda = { number: Int ->
        if (selectedDreamNumber != number) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        selectedDreamNumber = number
        keyboardController?.hide()
    }

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
    ) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Screen3DreamsPortrait(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedDreamNumber = selectedDreamNumber,
                onDreamItemClick = { onDreamItemClickLambda(it) },
                filteredDreamItems = filteredDreamItems,
                desiredOrbSize = desiredOrbSize,
                keyboardController = keyboardController
            )
        } else {
            Screen3DreamsLandscape(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedDreamNumber = selectedDreamNumber,
                onDreamItemClick = { onDreamItemClickLambda(it) },
                filteredDreamItems = filteredDreamItems,
                desiredOrbSize = desiredOrbSize,
                keyboardController = keyboardController
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Screen3DreamsPortrait(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedDreamNumber: Int?,
    onDreamItemClick: (Int) -> Unit,
    filteredDreamItems: List<DreamItem>,
    desiredOrbSize: Dp,
    keyboardController: SoftwareKeyboardController?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.dreams_oracle_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = LuckyGreen.darken(0.1f),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        SelectedNumberDisplay(
            selectedNumber = selectedDreamNumber,
            orbSize = desiredOrbSize
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 16.dp),
            placeholder = { Text(stringResource(id = R.string.dreams_search_placeholder_long), color = LuckyGreen.copy(alpha = 0.7f)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(id = R.string.dreams_search_cd), tint = LuckyGreen) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = stringResource(id = R.string.dreams_clear_cd), tint = LuckyGreen)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = LuckyGreen.darken(0.2f),
                unfocusedTextColor = LuckyGreen.darken(0.1f),
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                disabledContainerColor = Color.Transparent,
                cursorColor = GoldAccent,
                focusedBorderColor = GoldAccent.copy(alpha = 0.6f),
                unfocusedBorderColor = LuckyGreen.copy(alpha = 0.4f),
            ),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
        )

        DreamListContent(
            modifier = Modifier.weight(1f).padding(bottom = 16.dp),
            filteredDreamItems = filteredDreamItems,
            onDreamItemClick = onDreamItemClick
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Screen3DreamsLandscape(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedDreamNumber: Int?,
    onDreamItemClick: (Int) -> Unit,
    filteredDreamItems: List<DreamItem>,
    desiredOrbSize: Dp,
    keyboardController: SoftwareKeyboardController?
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
                .padding(end = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.dreams_oracle_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = LuckyGreen.darken(0.1f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SelectedNumberDisplay(
                selectedNumber = selectedDreamNumber,
                orbSize = desiredOrbSize
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                placeholder = { Text(stringResource(id = R.string.dreams_search_placeholder_short), color = LuckyGreen.copy(alpha = 0.7f)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(id = R.string.dreams_search_cd), tint = LuckyGreen) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(id = R.string.dreams_clear_cd), tint = LuckyGreen)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LuckyGreen.darken(0.2f),
                    unfocusedTextColor = LuckyGreen.darken(0.1f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    disabledContainerColor = Color.Transparent,
                    cursorColor = GoldAccent,
                    focusedBorderColor = GoldAccent.copy(alpha = 0.6f),
                    unfocusedBorderColor = LuckyGreen.copy(alpha = 0.4f),
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
            )
        }

        DreamListContent(
            modifier = Modifier.weight(0.55f).fillMaxHeight(),
            filteredDreamItems = filteredDreamItems,
            onDreamItemClick = onDreamItemClick
        )
    }
}

@Composable
fun DreamListContent(
    modifier: Modifier = Modifier,
    filteredDreamItems: List<DreamItem>,
    onDreamItemClick: (Int) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(modifier = Modifier.widthIn(max = 700.dp)) {
            if (filteredDreamItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = stringResource(id = R.string.dreams_no_results),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = LuckyGreen.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredDreamItems, key = { it.number }) { dreamItem ->
                        DreamListItemRedesigned(
                            dreamItem = dreamItem,
                            onItemClick = onDreamItemClick
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SelectedNumberDisplay(
    selectedNumber: Int?,
    orbSize: Dp = 120.dp
) {
    val density = LocalDensity.current
    val animatableScale = remember { Animatable(1.0f) }

    LaunchedEffect(selectedNumber) {
        if (selectedNumber != null) {
            animatableScale.animateTo(1.2f, spring(0.4f, 450f))
            animatableScale.animateTo(1.05f, spring(0.5f, 250f))
        } else {
            animatableScale.animateTo(1.0f, spring(0.5f, 250f))
        }
    }

    val numberFontSize: TextUnit = (orbSize.value * 0.42f).sp
    val emojiFontSize: TextUnit = (orbSize.value * 0.32f).sp

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(orbSize)
            .graphicsLayer {
                shadowElevation = with(density) { (orbSize.value * 0.125f).dp.toPx() }
                shape = CircleShape
                clip = true
                scaleX = animatableScale.value
                scaleY = animatableScale.value
            }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldAccent.copy(alpha = 0.7f),
                            GoldAccent.copy(alpha = 0.9f),
                            GoldAccent.darken(0.1f)
                        ),
                        radius = with(density) { orbSize.toPx() }
                    ),
                    shape = CircleShape
                )
                .blur(radius = (orbSize.value * 0.025f).dp)
        )
        Canvas(
            modifier = Modifier.matchParentSize()
        ) {
            val strokeWidthPx = with(density) { (orbSize.value * 0.033f).dp.toPx() }
            val innerStrokeWidthPx = with(density) { (orbSize.value * 0.016f).dp.toPx() }
            val innerRadius = this.size.minDimension / 2.0f - innerStrokeWidthPx * 1.5f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                    radius = this.size.minDimension * 0.6f,
                    center = this.center
                )
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = innerRadius,
                style = Stroke(width = innerStrokeWidthPx),
                center = this.center
            )
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        GoldAccent.copy(alpha = 0.9f),
                        GoldAccent.darken(0.35f),
                        GoldAccent.copy(alpha = 0.6f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(this.size.width, this.size.height)
                ),
                radius = this.size.minDimension / 2.0f,
                style = Stroke(width = strokeWidthPx),
                center = this.center
            )
        }
        Text(
            text = selectedNumber?.toString()?.padStart(2, '0') ?: stringResource(id = R.string.dreams_orb_placeholder),
            fontSize = if (selectedNumber != null) numberFontSize else emojiFontSize,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif,
            color = LuckyGreen.darken(0.3f),
            modifier = Modifier
                .graphicsLayer { alpha = 0.98f }
                .offset(y = (-2).dp)
        )
        if (selectedNumber != null) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val reflectionBaseSize = orbSize.value * 0.25f
                val reflectionSizePx = with(density) { reflectionBaseSize.dp.toPx() }
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f),
                    radius = reflectionSizePx / 1.8f,
                    center = Offset(this.size.width * 0.3f, this.size.height * 0.3f),
                    blendMode = BlendMode.Plus
                )
            }
        }
    }
}


@Composable
fun DreamListItemRedesigned(
    dreamItem: DreamItem,
    onItemClick: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, color = GoldAccent.copy(alpha = 0.5f)),
                onClick = { onItemClick(dreamItem.number) }
            )
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    LuckyGreen.copy(alpha = 0.3f),
                    GoldAccent.copy(alpha = 0.2f),
                    LuckyGreen.copy(alpha = 0.2f)
                )
            )
        ),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GoldAccent.copy(alpha = 0.4f),
                                LuckyGreen.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            radius = 52f * 0.8f
                        )
                    )
                    .border(1.dp, GoldAccent.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bedtime,
                    contentDescription = stringResource(id = R.string.dreams_icon_cd),
                    tint = GoldAccent.darken(0.1f),
                    modifier = Modifier
                        .size(34.dp)
                        .graphicsLayer {
                            rotationZ = if (dreamItem.number % 2 == 0) -7f else 7f
                            alpha = 0.95f
                        }
                )
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(GoldAccent.copy(alpha = 0.3f), CircleShape)
                            .align(Alignment.TopStart)
                            .offset(x = 6.dp, y = 6.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(GoldAccent.copy(alpha = 0.3f), CircleShape)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-6).dp, y = (-6).dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = dreamItem.text,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.animateContentSize()
                )
                Text(
                    text = dreamItem.meaning,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.animateContentSize()
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(id = R.string.dreams_details_cd),
                tint = GoldAccent.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}