import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

// --- Composable para el Número con Animación de Incremento y Pulso ---
// Este Composable muestra un número que primero cicla aleatoriamente,
// se fija en el valor objetivo, y luego realiza una pequeña animación de pulso.
@Composable
fun AnimatedIncrementingNumber(
    targetValue: Int, // El valor final que debe mostrar el número.
    index: Int, // Índice para escalonar la animación (cada número empieza con un retraso).
    textStyle: TextStyle, // Estilo del texto a aplicar al número.
    textColor: Color, // Color del texto del número.
    modifier: Modifier = Modifier // Modifier base para el contenedor del número (el círculo).
) {
    // Estado mutable para el valor que se muestra. Empieza en 0.
    var displayedValue by remember { mutableStateOf(0) }
    // Estado para controlar el inicio de la animación de pulso post-incremento.
    var startPulse by remember { mutableStateOf(false) }

    // Efecto lanzado que se ejecuta cuando targetValue cambia.
    LaunchedEffect(key1 = targetValue) {
        // 1. Retraso inicial: Aumenta el retraso según el índice para escalonar la animación.
        delay(index * 250L) // Retraso de 250ms por cada número.

        // 2. Fase de ciclado rápido: El número cambia aleatoriamente por un tiempo.
        val animationDuration = 800L // Duración total de la fase de ciclado rápido.
        val updateInterval = 60L    // Intervalo entre cada cambio de dígito aleatorio.
        val startTime = System.currentTimeMillis() // Marca de tiempo de inicio.

        // Bucle que cambia el valor mostrado a un número aleatorio hasta que pasa el tiempo de duración.
        while (System.currentTimeMillis() < startTime + animationDuration) {
            displayedValue = Random.nextInt(0, 10) // Número aleatorio entre 0 y 9.
            delay(updateInterval) // Espera antes del siguiente cambio.
        }

        // 3. Fijar el valor final: Una vez terminada la fase de ciclado, se muestra el valor objetivo.
        displayedValue = targetValue

        // 4. Iniciar la animación de pulso después de un pequeño retraso.
        delay(100) // Pequeño retraso antes del pulso.
        startPulse = true
    }

    // Objeto Animatable para controlar la animación de escala del pulso.
    val pulseScale = remember { Animatable(1.0f) } // Empieza en escala normal (1.0).

    // Efecto lanzado que se activa cuando startPulse cambia a true.
    LaunchedEffect(startPulse) {
        if (startPulse) {
            // Anima la escala: aumenta ligeramente y luego vuelve a la normalidad.
            pulseScale.animateTo(
                targetValue = 1.1f, // Escala ligeramente (110%).
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing) // Animación rápida de subida.
            )
            pulseScale.animateTo(
                targetValue = 1.0f, // Vuelve a la escala normal.
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing) // Animación más lenta de bajada.
            )
            // Si quisieras que el pulso solo ocurra una vez, podrías resetear startPulse aquí:
            // startPulse = false
        }
    }

    // Contenedor del número (el círculo dorado) - se aplica el modifier pasado aquí.
    Box(
        // Aplicar el modifier pasado (tamaño, fondo, sombra) y transformaciones gráficas animadas.
        modifier = modifier
            .graphicsLayer { // Aplicar transformaciones gráficas como escala y alfa.
                // Usar el valor animado de 'pulseScale' para la escala.
                scaleX = pulseScale.value
                scaleY = pulseScale.value
                // Opcional: Podrías añadir una animación de alpha aquí si quisieras que también pulsara la transparencia.
                // alpha = pulseAlpha.value // Si tuvieras una animación de alpha separada.
            },
        contentAlignment = Alignment.Center // Centrar el contenido (el Text) dentro del Box.
    ) {
        // Composable Text para mostrar el valor animado.
        Text(
            text = "$displayedValue", // Muestra el valor que está cambiando/animado.
            style = textStyle, // Aplica el estilo de texto pasado (tamaño, etc.).
            color = textColor, // Aplica el color de texto pasado.
            fontWeight = FontWeight.ExtraBold // Aplica el peso de fuente en negrita.
        )
    }
}

// --- Pantalla Principal: Lucky Numbers Modern ---
@Composable
fun LuckyNumbersScreenModern(modifier: Modifier = Modifier) {
    // --- Estados de la pantalla ---
    // Controla si el botón para obtener números está habilitado.
    var canGetNumbers by remember { mutableStateOf(true) }
    // Controla si la animación de carga está activa.
    var isAnimating by remember { mutableStateOf(false) }
    // La lista de números de la suerte generados (nullable porque inicialmente no hay números).
    var luckyNumbers by remember { mutableStateOf<List<Int>?>(null) }

    // --- Paleta de Colores Moderna ---
    // Definición de los colores utilizados en la UI.
    val deepGreen = Color(0xFF004D40) // Verde oscuro.
    val vibrantGreen = Color(0xFF00796B) // Verde vibrante.
    val goldAccent = Color(0xFFFFC107) // Dorado para acentos.
    val softGold = Color(0xFFFFECB3) // Dorado suave/claro.
    val offWhite = Color(0xFFFFF8E1) // Blanco hueso para la tarjeta.
    val textPrimary = Color.White // Color principal del texto (en fondo oscuro).
    val textSecondary = Color.White.copy(alpha = 0.8f) // Color secundario del texto (transparencia).
    val textOnGold = Color(0xFF4E342E) // Color de texto oscuro para usar sobre dorado.

    // --- Animación Icono Carga (Rotación) ---
    // Transición infinita para animaciones que se repiten.
    val infiniteTransition = rememberInfiniteTransition(label = "icon rotation transition")
    // Valor animado para el ángulo de rotación del icono principal.
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f, // Rota de 0 a 360 grados.
        animationSpec = infiniteRepeatable( // Se repite infinitamente.
            animation = tween(1500, easing = LinearEasing), // Animación lineal de 1.5 segundos.
            repeatMode = RepeatMode.Restart // Reinicia la animación al terminar.
        ), label = "icon rotation angle"
    )

    // --- Animación Placeholder '?' (Pulso Alpha) ---
    // Valor animado para la transparencia (alpha) del placeholder '?'.
    val placeholderPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1.0f, // Pulsa entre 60% y 100% de opacidad.
        animationSpec = infiniteRepeatable( // Se repite infinitamente.
            animation = tween(800, easing = FastOutSlowInEasing), // Animación de 0.8 segundos con easing.
            repeatMode = RepeatMode.Reverse // Revierte la animación al terminar (pulso).
        ), label = "placeholder pulse"
    )

    // --- Lógica de Generación de Números ---
    // Efecto lanzado que se activa cuando el estado 'isAnimating' cambia.
    LaunchedEffect(key1 = isAnimating) {
        // Si isAnimating es true, iniciamos el proceso de generación.
        if (isAnimating) {
            delay(2500) // Espera 2.5 segundos (simulando un proceso de carga).
            luckyNumbers = List(3) { Random.nextInt(1, 10) } // Genera una lista de 3 números aleatorios (1 a 9).
            isAnimating = false // Desactiva el estado de carga.
            canGetNumbers = false // Deshabilita el botón después de generar (opcional).
        }
    }

    // Función para iniciar el proceso de generación de números.
    fun generateNumbers() {
        // Solo permite generar si el botón está habilitado y no está ya animando.
        if (canGetNumbers && !isAnimating) {
            isAnimating = true // Activa el estado de carga/animación.
            luckyNumbers = null // Resetea los números anteriores.
        }
    }

    // --- Interfaz de Usuario (UI) ---
    Box(
        // Modifier principal para el contenedor de toda la pantalla.
        modifier = modifier
            .fillMaxSize() // Ocupa todo el espacio disponible.
            .background( // Aplica un fondo degradado radial.
                brush = Brush.radialGradient(
                    colors = listOf(vibrantGreen, deepGreen), // Colores del degradado.
                    center = Offset(0.5f, 0.5f), // Centro del degradado (en medio de la pantalla).
                    radius = 900f // Radio del degradado.
                )
            )
            .padding(horizontal = 20.dp, vertical = 32.dp) // Añade padding horizontal y vertical.
    ) {
        // Columna principal que organiza los elementos verticalmente.
        Column(
            modifier = Modifier.fillMaxSize(), // Ocupa todo el espacio del Box padre.
            verticalArrangement = Arrangement.SpaceAround, // Distribuye el espacio verticalmente.
            horizontalAlignment = Alignment.CenterHorizontally // Centra los elementos horizontalmente.
        ) {
            // --- Encabezado ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Icono principal (Favorite).
                Icon(
                    imageVector = Icons.Filled.Favorite, // Usa el icono Favorite.
                    contentDescription = "Icono de Suerte", // Descripción para accesibilidad.
                    tint = goldAccent, // Color del icono.
                    modifier = Modifier
                        .size(72.dp) // Tamaño del icono.
                        .graphicsLayer { if (isAnimating) { rotationZ = rotationAngle } } // Aplica rotación si está animando.
                        .padding(bottom = 8.dp) // Padding inferior.
                )
                // Título principal.
                Text( "Tus Números de la Suerte",
                    style = MaterialTheme.typography.headlineMedium, // Estilo de texto.
                    fontWeight = FontWeight.Bold, color = textPrimary, textAlign = TextAlign.Center, // Fuente, color, alineación.
                    modifier = Modifier.padding(horizontal = 16.dp) // Padding horizontal.
                )
                Spacer(modifier = Modifier.height(8.dp)) // Espacio vertical.
                // Subtítulo.
                Text( "¡Descubre tu fortuna diaria!",
                    style = MaterialTheme.typography.bodyLarge, color = textSecondary, textAlign = TextAlign.Center // Estilo, color, alineación.
                )
            } // Fin Encabezado

            // --- Tarjeta de Números ---
            Card(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), // Ocupa el ancho máximo, altura mínima intrínseca.
                shape = RoundedCornerShape(28.dp), // Forma con esquinas redondeadas.
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Elevación de la tarjeta (sombra).
                colors = CardDefaults.cardColors(containerColor = offWhite.copy(alpha = 0.9f)) // Color de fondo con transparencia.
            ) {
                // Box interno para padding y centrado del contenido de la tarjeta.
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 40.dp), // Padding interno.
                    contentAlignment = Alignment.Center // Centra el contenido.
                ) {
                    // AnimatedContent para animar la transición entre los diferentes estados del contenido de la tarjeta.
                    AnimatedContent(
                        // Define el estado actual basado en las variables isAnimating y luckyNumbers.
                        targetState = when {
                            isAnimating -> "Loading" // Si está animando, estado "Loading".
                            luckyNumbers != null -> "Numbers" // Si hay números, estado "Numbers".
                            else -> "Initial" // Si no, estado "Initial".
                        },
                        // Define cómo se animan las transiciones entre estados.
                        transitionSpec = {
                            // Animación de entrada (aparece): FadeIn y ScaleIn.
                            fadeIn(animationSpec = tween(300, 50)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300, 50)) togetherWith
                                    // Animación de salida (desaparece): FadeOut y ScaleOut.
                                    fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.92f, animationSpec = tween(150))
                        },
                        label = "Card Content Transition" // Etiqueta para la animación.
                    ) { state -> // El contenido a mostrar basado en el estado actual.
                        when (state) {
                            "Loading" -> { // Estado de carga con '?' pulsantes.
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally), // Espacio entre elementos.
                                    modifier = Modifier.fillMaxWidth() // Ocupa el ancho máximo.
                                ) {
                                    repeat(3) { // Crea 3 elementos (uno por cada número esperado).
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp) // Tamaño del círculo.
                                                .background(softGold.copy(alpha = placeholderPulse), CircleShape) // Fondo dorado suave con alpha animado.
                                                .padding(8.dp), // Padding interno.
                                            contentAlignment = Alignment.Center // Centra el contenido ('?').
                                        ) {
                                            Text( "?", // Muestra el signo de interrogación.
                                                style = MaterialTheme.typography.headlineMedium, // Estilo de texto.
                                                color = goldAccent.copy(alpha = placeholderPulse), // Color dorado con alpha animado.
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            "Numbers" -> { // Estado con números revelados
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally), // Espacio entre números.
                                    modifier = Modifier.fillMaxWidth() // Ocupa el ancho máximo.
                                ) {
                                    // Itera sobre la lista de números de la suerte y muestra cada uno.
                                    luckyNumbers?.forEachIndexed { index, number ->
                                        // Definir el modifier base para el contenedor de cada número (el círculo dorado).
                                        val numberBoxModifier = Modifier
                                            .size(72.dp) // Tamaño del círculo del número.
                                            .background( // Fondo degradado radial para el círculo.
                                                brush = Brush.radialGradient(
                                                    colors = listOf(goldAccent, softGold), // Colores dorado.
                                                    radius = 40f
                                                ),
                                                shape = CircleShape // **AQUÍ SE ESPECIFICA CircleShape**
                                            )
                                            .shadow(4.dp, CircleShape) // **Y aquí también**

                                        // Llama al Composable animado para mostrar el número.
                                        AnimatedIncrementingNumber(
                                            targetValue = number, // El número final a mostrar.
                                            index = index, // El índice para el escalonamiento de la animación.
                                            textStyle = MaterialTheme.typography.headlineLarge, // Estilo del texto del número.
                                            textColor = textOnGold, // Color del texto del número.
                                            modifier = numberBoxModifier // Pasa el modifier definido.
                                        )
                                    }
                                }
                            }
                            "Initial" -> { // Estado inicial antes de generar números.
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally, // Centra horizontalmente.
                                    verticalArrangement = Arrangement.Center, // Centra verticalmente.
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp) // Ocupa ancho máximo, padding vertical.
                                ) {
                                    // Icono en el estado inicial.
                                    Icon( Icons.Filled.FavoriteBorder,
                                        contentDescription = "Esperando suerte", // Descripción accesibilidad.
                                        tint = goldAccent.copy(alpha = 0.8f), modifier = Modifier.size(50.dp) // Color y tamaño.
                                    )
                                    Spacer(modifier = Modifier.height(16.dp)) // Espacio.
                                    // Texto de instrucción.
                                    Text( "Pulsa el botón para revelar tu destino",
                                        style = MaterialTheme.typography.bodyLarge, color = vibrantGreen, // Estilo y color.
                                        textAlign = TextAlign.Center, fontWeight = FontWeight.Medium // Alineación y fuente.
                                    )
                                }
                            }
                        }
                    }
                }
            } // Fin Tarjeta

            // --- Botón de Acción ---
            Button(
                onClick = { generateNumbers() }, // Acción al hacer clic.
                enabled = canGetNumbers && !isAnimating, // Habilitado solo si canGetNumbers es true y no está animando.
                modifier = Modifier.fillMaxWidth(0.85f).height(64.dp), // Tamaño del botón.
                shape = CircleShape, // Forma circular.
                colors = ButtonDefaults.buttonColors( // Colores del botón (normal y deshabilitado).
                    containerColor = goldAccent, contentColor = textOnGold,
                    disabledContainerColor = softGold.copy(alpha = 0.5f),
                    disabledContentColor = textOnGold.copy(alpha = 0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation( // Elevación del botón (sombra).
                    defaultElevation = 6.dp, pressedElevation = 8.dp, disabledElevation = 2.dp
                )
            ) {
                // AnimatedContent para animar la transición del contenido del botón (icono y texto).
                AnimatedContent(
                    // El estado se basa en si está animando, si puede obtener números y si ya se revelaron.
                    targetState = Triple(isAnimating, canGetNumbers, luckyNumbers != null),
                    transitionSpec = {
                        // Define la transición: si el estado de animación cambia, usa slide vertical; si no, fade.
                        if (initialState.first != targetState.first) {
                            (slideInVertically { h -> h } + fadeIn() togetherWith slideOutVertically { h -> -h } + fadeOut()).using(SizeTransform(clip = false))
                        } else {
                            fadeIn(animationSpec = tween(200, 100)) togetherWith fadeOut(animationSpec = tween(100))
                        }
                    },
                    label = "Button Text/Icon Animation" // Etiqueta de la animación.
                ) { (loading, canGet, revealed) -> // Contenido basado en el estado del botón.
                    Row( verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center ) {
                        if (loading) {
                            // Muestra un indicador de progreso si está cargando.
                            CircularProgressIndicator( color = textOnGold, modifier = Modifier.size(28.dp), strokeWidth = 3.dp )
                        } else {
                            // Define el icono y el texto basado en si los números ya fueron revelados.
                            val icon: ImageVector = if (revealed) Icons.Filled.CheckCircle else Icons.Filled.Star
                            val text: String = if (revealed) "NÚMEROS REVELADOS" else "OBTENER NÚMEROS"
                            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp)) // Muestra el icono.
                            Spacer(modifier = Modifier.width(12.dp)) // Espacio entre icono y texto.
                            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold ) // Muestra el texto del botón.
                        }
                    }
                }
            } // Fin Botón

            // --- Mensaje Final ---
            Text( "¡Mucha suerte!", style = MaterialTheme.typography.bodyMedium,
                color = textSecondary.copy(alpha = 0.7f), modifier = Modifier.padding(top = 8.dp) // Estilo, color, padding.
            )

        } // Fin Columna Principal

        // --- Elementos Decorativos Visibles ---
        // Estos son Boxes con blur y background para añadir un efecto visual de orbes.
        Box(
            Modifier
                .align(Alignment.BottomEnd) // Alineado a la parte inferior derecha.
                .offset(x = 30.dp, y = 30.dp) // Desplazamiento desde la esquina.
                .size(200.dp) // Tamaño.
                .blur(radius = 40.dp) // Aplica un efecto de desenfoque.
                .background(softGold.copy(alpha = 0.25f), CircleShape) // Fondo dorado suave con transparencia y forma circular.
        )
        Box(
            Modifier
                .align(Alignment.TopStart) // Alineado a la parte superior izquierda.
                .offset(x = (-40).dp, y = (-40).dp) // Desplazamiento.
                .size(200.dp) // Tamaño.
                .blur(radius = 30.dp) // Desenfoque.
                .background(softGold.copy(alpha = 0.20f), CircleShape) // Fondo con transparencia y forma circular.
        )

    } // Fin Box principal
}