package com.matancita.loteria.ui.theme.screen // O el nombre de tu paquete

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import android.content.res.Configuration // Para detectar orientación
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.SoftwareKeyboardController

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
val allDreamExperiencesGlobal = listOf(
    DreamItem(1, "Agua", "Soñar con agua generalmente se asocia con las emociones, la limpieza espiritual o el flujo de la vida."),
    DreamItem(2, "Un pato", "Este sueño puede reflejar adaptabilidad, equilibrio entre emociones y racionalidad."),
    DreamItem(3, "Un niño", "Representa inocencia, nuevos comienzos o aspectos vulnerables del soñador."),
    DreamItem(4, "Un hombre", "Puede reflejar autoridad, figura paternal o aspectos del yo masculino."),
    DreamItem(5, "Una mujer", "Representa intuición, emociones o figura materna."),
    DreamItem(6, "Gato", "Soñar con un gato puede simbolizar independencia, misterio o intuición."),
    DreamItem(7, "Perro", "Significa lealtad, amistad o protección."),
    DreamItem(8, "Una culebra o serpiente", "Puede implicar transformación, miedo oculto o traición."),
    DreamItem(9, "Incendio", "Sugiere destrucción, pasión intensa o renovación."),
    DreamItem(10, "Un muerto", "Simboliza el cierre de un ciclo o algo del pasado que regresa."),
    DreamItem(11, "Arroyo", "Asociado al fluir de la vida, calma o decisiones menores."),
    DreamItem(12, "Un cañon", "Puede representar fuerza, peligro inminente o eventos explosivos."),
    DreamItem(13, "La leche", "Simboliza nutrición, abundancia o cuidado maternal."),
    DreamItem(14, "Un árbol", "Representa crecimiento, estabilidad o conexión con la naturaleza."),
    DreamItem(15, "Palito", "Podría indicar cosas simples, frágiles o detalles insignificantes que tienen valor."),
    DreamItem(16, "Un soldado", "Simboliza disciplina, defensa personal o lucha interna."),
    DreamItem(17, "Un gato negro", "Asociado con supersticiones, misterio o intuición intensificada."),
    DreamItem(18, "Un borracho", "Puede sugerir falta de control o evasión de la realidad."),
    DreamItem(19, "Una quinceañera", "Refleja transición, celebración o juventud femenina."),
    DreamItem(20, "Niña bonita", "Representa belleza inocente, ternura o momentos felices."),
    DreamItem(21, "Un anillo", "Significa compromiso, ciclo completo o unión."),
    DreamItem(22, "Pescados", "Abundancia, oportunidades o deseos inconscientes."),
    DreamItem(23, "La fiesta", "Alegría, celebración o necesidad de socialización."),
    DreamItem(24, "Un río", "Simboliza emociones profundas, transiciones o flujo de vida."),
    DreamItem(25, "La mujer", "Elemento femenino, sensibilidad o conexión emocional."),
    DreamItem(26, "Patitos", "Inocencia, protección materna o seguimiento."),
    DreamItem(27, "Un cocinero", "Nutrición, preparación o creatividad en el entorno cotidiano."),
    DreamItem(28, "Caballos", "Fuerza, libertad o instintos salvajes."),
    DreamItem(29, "Un fantasma", "Culpa, pasado sin resolver o miedos ocultos."),
    DreamItem(30, "Una misa", "Espiritualidad, reflexión o comunidad."),
    DreamItem(31, "Un loco", "Descontrol, creatividad desbordada o conflicto interior."),
    DreamItem(32, "Un espejo", "Autoevaluación, verdad interna o percepción."),
    DreamItem(33, "Flores", "Belleza, crecimiento emocional o reconocimiento."),
    DreamItem(34, "Luz", "Iluminación, comprensión o esperanza."),
    DreamItem(35, "Mucho dinero", "Deseo de abundancia, poder o seguridad."),
    DreamItem(36, "Mauras (aves negras)", "Presagios, miedos o misterios ocultos."),
    DreamItem(37, "La cabeza", "Pensamientos, razonamiento o identidad."),
    DreamItem(38, "Un pajarito", "Mensajes, esperanza o libertad."),
    DreamItem(39, "Queso", "Recompensa, satisfacción o aspectos nutritivos."),
    DreamItem(40, "Un dentista", "Cuidado personal, dolor reprimido o preocupación por la imagen."),
    DreamItem(41, "Piedras", "Obstáculos, firmeza o cargas emocionales."),
    DreamItem(42, "Lluvia", "Limpieza emocional, tristeza o renovación."),
    DreamItem(43, "Bailando", "Alegría, expresión o libertad personal."),
    DreamItem(44, "Un cuchillo", "Conflictos, traición o decisiones difíciles."),
    DreamItem(45, "Prendas", "Identidad, protección o expresión externa."),
    DreamItem(46, "Un maco o sapo", "Transformación, aspectos ocultos o limpieza emocional."),
    DreamItem(47, "La cárcel", "Limitación, culpa o restricciones personales."),
    DreamItem(48, "Una pistola", "Peligro, defensa o agresión contenida."),
    DreamItem(49, "Tomate", "Vitalidad, pasión o alimentación saludable."),
    DreamItem(50, "Santos", "Protección espiritual, fe o guía divina."),
    DreamItem(51, "Reviviendo", "Renacimiento, segunda oportunidad o lecciones pasadas."),
    DreamItem(52, "Carne", "Deseos básicos, energía o necesidad física."),
    DreamItem(53, "Pan", "Sustento, simplicidad o necesidades básicas."),
    DreamItem(54, "Un serrucho", "Trabajo, esfuerzo o necesidad de cortar con algo."),
    DreamItem(55, "Un barco", "Viajes, emociones profundas o tránsito personal."),
    DreamItem(56, "Vacas", "Abundancia, calma o fertilidad."),
    DreamItem(57, "Música", "Expresión emocional, armonía o inspiración."),
    DreamItem(58, "Una caída", "Fracaso, pérdida de control o inseguridad."),
    DreamItem(59, "Un jorobado", "Carga emocional, complejos o dificultad superada."),
    DreamItem(60, "Un abogado", "Justicia, decisiones legales o búsqueda de equilibrio."),
    DreamItem(61, "Matas (plantas)", "Crecimiento, naturaleza o renovación constante."),
    DreamItem(62, "Una escuela", "Aprendizaje, desarrollo personal o pasado."),
    DreamItem(63, "Una escopeta", "Defensa, poder o acción contundente."),
    DreamItem(64, "Una inundación", "Emociones abrumadoras, crisis o renovación forzada."),
    DreamItem(65, "Madre e hija", "Relaciones, protección y vínculos emocionales fuertes."),
    DreamItem(66, "Llanto", "Liberación emocional, tristeza o necesidad de desahogo."),
    DreamItem(67, "Una boda", "Unión, compromiso o nuevas etapas."),
    DreamItem(68, "Mujeres", "Energía femenina, relaciones o intuición múltiple."),
    DreamItem(69, "Una mordida", "Ataque, traición o deseo intenso."),
    DreamItem(70, "Víbora", "Amenaza, traición o energía sexual reprimida."),
    DreamItem(71, "Sobrinos", "Vínculos familiares, juventud o responsabilidad emocional."),
    DreamItem(72, "Un limosnero", "Carencia, humildad o necesidad de ayuda."),
    DreamItem(73, "Un jarrón", "Belleza, fragilidad o contención emocional."),
    DreamItem(74, "Lesionado", "Dolor emocional, heridas internas o vulnerabilidad."),
    DreamItem(75, "Un moreno", "Diversidad, fuerza o conexión cultural."),
    DreamItem(76, "Un payaso", "Falsedad, alegría fingida o confusión emocional."),
    DreamItem(77, "Llamas o fuego", "Transformación, pasión intensa o advertencia."),
    DreamItem(78, "Muletas", "Apoyo, dependencia o recuperación."),
    DreamItem(79, "Pelotas", "Juego, energía juvenil o habilidad para adaptarse."),
    DreamItem(80, "Matemáticas", "Razonamiento, lógica o resolución de problemas."),
    DreamItem(81, "Flores", "Simboliza belleza, emociones positivas o reconocimiento."),
    DreamItem(82, "Peleas", "Conflictos internos o con otros, lucha por control."),
    DreamItem(83, "Felicidad", "Estado ideal, bienestar emocional o meta personal."),
    DreamItem(84, "Una iglesia", "Fe, guía moral o refugio espiritual."),
    DreamItem(85, "Una letrina", "Deseo de liberarse de cargas emocionales o aspectos vergonzosos."),
    DreamItem(86, "Un turista", "Exploración, nuevas experiencias o desconexión."),
    DreamItem(87, "Piojos", "Preocupación, molestias pequeñas o pensamientos invasivos."),
    DreamItem(88, "El papa", "Autoridad espiritual, guía o respeto."),
    DreamItem(89, "Ratones", "Temores pequeños, preocupaciones o falta de confianza."),
    DreamItem(90, "Miedo", "Inseguridad, advertencia o confrontación emocional."),
    DreamItem(91, "Un pintor", "Creatividad, expresión artística o percepción."),
    DreamItem(92, "Excusado", "Eliminación de lo innecesario, limpieza emocional."),
    DreamItem(93, "Un médico", "Curación, ayuda o necesidad de cuidado."),
    DreamItem(94, "Te soñaste enamorado de alguien", "Deseo, idealización o emociones no resueltas."),
    DreamItem(95, "Un cementerio", "Cierre de ciclos, recuerdos o transformación."),
    DreamItem(96, "Los lentes", "Claridad, perspectiva o necesidad de ver mejor una situación."),
    DreamItem(97, "Un tren", "Destino, planificación o eventos incontrolables."),
    DreamItem(98, "Una mesa", "Unidad, diálogo o sustento común."),
    DreamItem(99, "La bandera", "Identidad, patriotismo o lealtad."),
    DreamItem(100, "Hermanos", "Relaciones familiares, unión o aspectos del yo reflejados en otros.")
)
// --- Fin de definiciones ---


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Screen3Dreams() {
    val allDreamExperiences = remember { allDreamExperiencesGlobal }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDreamNumber by remember { mutableStateOf<Int?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current

    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

    // Ajustar el tamaño del orbe según la orientación y dimensiones
    val desiredOrbSize = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        (configuration.screenWidthDp.dp * 0.33f).coerceIn(100.dp, 150.dp)
    } else { // Landscape
        // En landscape, basarlo en la altura o un porcentaje menor del ancho puede ser mejor
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

    Box( // Contenedor raíz con el fondo
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
                onDreamItemClick = {onDreamItemClickLambda(it)},
                filteredDreamItems = filteredDreamItems,
                desiredOrbSize = desiredOrbSize,
                keyboardController = keyboardController
            )
        } else {
            Screen3DreamsLandscape(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedDreamNumber = selectedDreamNumber,
                onDreamItemClick = {onDreamItemClickLambda(it)},
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
    keyboardController: SoftwareKeyboardController? // Mantenido por si OutlinedTextField lo necesita
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp), // Padding horizontal general
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Oráculo de Sueños",
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
            placeholder = { Text("Buscar sueño por palabra o número...", color = LuckyGreen.copy(alpha = 0.7f)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = LuckyGreen) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Limpiar", tint = LuckyGreen)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors( /* ... mismos colores que antes ... */
                focusedTextColor = LuckyGreen.darken(0.2f),
                unfocusedTextColor = LuckyGreen.darken(0.1f),
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                disabledContainerColor = Color.Transparent,
                cursorColor = GoldAccent,
                focusedBorderColor = GoldAccent.copy(alpha = 0.6f),
                unfocusedBorderColor = LuckyGreen.copy(alpha = 0.4f),
                focusedLeadingIconColor = GoldAccent,
                unfocusedLeadingIconColor = LuckyGreen.copy(alpha = 0.8f),
                focusedTrailingIconColor = GoldAccent,
                unfocusedTrailingIconColor = LuckyGreen.copy(alpha = 0.8f),
                focusedPlaceholderColor = LuckyGreen.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = LuckyGreen.copy(alpha = 0.7f)
            ),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
        )

        DreamListContent( // Extraído a un Composable separado para reutilizar
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
            .padding(horizontal = 16.dp, vertical = 16.dp), // Padding general para landscape
        verticalAlignment = Alignment.Top // Alinear los paneles al tope
    ) {
        // --- Panel Izquierdo (Título, Orbe, Búsqueda) ---
        Column(
            modifier = Modifier
                .weight(0.45f) // Porcentaje del ancho para este panel
                .fillMaxHeight()
                .padding(end = 8.dp) // Espacio entre paneles
                .verticalScroll(rememberScrollState()), // Hacer este panel scrolleable si su contenido es muy alto
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp)) // Menor espacio superior en landscape
            Text(
                "Oráculo de Sueños",
                fontSize = 28.sp, // Ligeramente más pequeño para landscape
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = LuckyGreen.darken(0.1f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SelectedNumberDisplay(
                selectedNumber = selectedDreamNumber,
                orbSize = desiredOrbSize // El tamaño ya está adaptado para landscape
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                placeholder = { Text("Buscar...", color = LuckyGreen.copy(alpha = 0.7f)) }, // Placeholder más corto
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = LuckyGreen) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpiar", tint = LuckyGreen)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors( /* ... mismos colores que antes ... */
                    focusedTextColor = LuckyGreen.darken(0.2f),
                    unfocusedTextColor = LuckyGreen.darken(0.1f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    disabledContainerColor = Color.Transparent,
                    cursorColor = GoldAccent,
                    focusedBorderColor = GoldAccent.copy(alpha = 0.6f),
                    unfocusedBorderColor = LuckyGreen.copy(alpha = 0.4f),
                    focusedLeadingIconColor = GoldAccent,
                    unfocusedLeadingIconColor = LuckyGreen.copy(alpha = 0.8f),
                    focusedTrailingIconColor = GoldAccent,
                    unfocusedTrailingIconColor = LuckyGreen.copy(alpha = 0.8f),
                    focusedPlaceholderColor = LuckyGreen.copy(alpha = 0.5f),
                    unfocusedPlaceholderColor = LuckyGreen.copy(alpha = 0.7f)
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
            )
        }

        // --- Panel Derecho (Lista de Sueños) ---
        DreamListContent(
            modifier = Modifier.weight(0.55f).fillMaxHeight(), // Ocupa el resto del espacio
            filteredDreamItems = filteredDreamItems,
            onDreamItemClick = onDreamItemClick
        )
    }
}

// Composable extraído para la lista de sueños y el mensaje de "no resultados"
@Composable
fun DreamListContent(
    modifier: Modifier = Modifier,
    filteredDreamItems: List<DreamItem>,
    onDreamItemClick: (Int) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth(), // Ocupa el ancho del panel/pantalla asignado
        contentAlignment = Alignment.TopCenter
    ) {
        Box(modifier = Modifier.widthIn(max = 700.dp)) { // Mantenemos la restricción de ancho
            if (filteredDreamItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        "No se encontraron sueños que coincidan.",
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


// El Composable SelectedNumberDisplay modificado para aceptar orbSize ya se proporcionó
// y debería estar en este archivo o importado. Lo incluyo aquí por completitud.
@Composable
fun SelectedNumberDisplay(
    selectedNumber: Int?,
    orbSize: Dp = 120.dp
) {
    val density = LocalDensity.current
    val animatableScale = remember { Animatable(1.0f) }

    LaunchedEffect(selectedNumber) {
        if (selectedNumber != null) {
            animatableScale.animateTo(
                targetValue = 1.2f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 450f)
            )
            animatableScale.animateTo(
                targetValue = 1.05f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 250f)
            )
        } else {
            animatableScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 250f)
            )
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
            text = selectedNumber?.toString()?.padStart(2, '0') ?: "🔮",
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

// El Composable DreamListItemRedesigned se mantiene como lo tenías.
// Lo incluyo aquí para que el archivo esté completo.
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
                    contentDescription = "Ícono de sueño",
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
                            .background(GoldAccent.copy(alpha = 0.3f))
                            .align(Alignment.TopStart)
                            .offset(x = 6.dp, y = 6.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(GoldAccent.copy(alpha = 0.3f))
                            .align(Alignment.BottomEnd)
                            .offset(x = (-6).dp, y = (-6).dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = dreamItem.text.substringAfter(":").trimStart(),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 23.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.animateContentSize()
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Ver detalle",
                tint = GoldAccent.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
