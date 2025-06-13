package com.matancita.loteria.ui.theme.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Looks3
import androidx.compose.material.icons.filled.Looks5
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.matancita.loteria.R
import com.matancita.loteria.ui.theme.nav.AppDestinations
import com.matancita.loteria.viewmodel.HoroscopeViewModel
import com.matancita.loteria.viewmodel.NumbersViewModel
import com.matancita.loteria.viewmodel.UserDataViewModel

// --- Modelos de Navegación y Datos ---

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    @StringRes val labelResId: Int
) {
    object Screen1 : BottomNavItem(AppDestinations.SCREEN_1_NUMBERS, Icons.Filled.Looks3, R.string.tab_suerte)
    object Screen2 : BottomNavItem(AppDestinations.SCREEN_2_NUMBERS, Icons.Filled.Looks5, R.string.tab_chance)
    // Se usa AutoAwesome para el horóscopo, que encaja mejor con la temática astral.
    object Screen4 : BottomNavItem(AppDestinations.SCREEN_4_HOROSCOPE, Icons.Default.AutoAwesome, R.string.tab_horoscope)
    object Screen3 : BottomNavItem(AppDestinations.SCREEN_3_DREAMS, Icons.Filled.Favorite, R.string.tab_dreams)
}

// --- Layout Principal Mágico ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenLayout(
    navController: NavController,
    userDataViewModel: UserDataViewModel = viewModel(),
    numbersViewModel: NumbersViewModel = viewModel(),
    horoscopeViewModel: HoroscopeViewModel = viewModel()
) {
    val bottomNavController = rememberNavController()
    val userProfile by userDataViewModel.userProfile.collectAsState()

    LaunchedEffect(userProfile) {
        userProfile?.let {
            numbersViewModel.loadNumbersForScreen("screen1", it)
            numbersViewModel.loadNumbersForScreen("screen2", it)
        }
    }

    // El fondo ahora es manejado por cada pantalla individualmente,
    // por lo que el Scaffold puede ser transparente.
    Scaffold(
        bottomBar = {
            MagicalBottomNavigationBar(
                navController = bottomNavController,
                // Colores celestiales para la barra de navegación
                containerColor = Color(0xFF0c0c2b).copy(alpha = 0.95f),
                selectedContentColor = Color.White,
                unselectedContentColor = Color.White.copy(alpha = 0.7f),
                indicatorColor = Color.White.copy(alpha = 0.15f)
            )
        },
        containerColor = Color.Transparent // El fondo lo provee StarryNightBackground en cada pantalla
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            MagicalAppNavigationHost(
                navController = bottomNavController,
                userDataViewModel = userDataViewModel,
                numbersViewModel = numbersViewModel,
                horoscopeViewModel = horoscopeViewModel
            )
        }
    }
}

@Composable
fun MagicalBottomNavigationBar(
    navController: NavHostController,
    containerColor: Color,
    selectedContentColor: Color,
    unselectedContentColor: Color,
    indicatorColor: Color
) {
    val items = listOf(
        BottomNavItem.Screen1,
        BottomNavItem.Screen2,
        BottomNavItem.Screen4, // Horóscopo
        BottomNavItem.Screen3
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier.border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))),
        containerColor = containerColor,
        contentColor = unselectedContentColor
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            val labelText = stringResource(id = item.labelResId)
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = labelText) },
                label = { Text(labelText, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp) },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedContentColor,
                    selectedTextColor = selectedContentColor,
                    unselectedIconColor = unselectedContentColor,
                    unselectedTextColor = unselectedContentColor,
                    indicatorColor = indicatorColor
                ),
                alwaysShowLabel = true
            )
        }
    }
}

@Composable
fun MagicalAppNavigationHost(
    navController: NavHostController,
    userDataViewModel: UserDataViewModel,
    numbersViewModel: NumbersViewModel,
    horoscopeViewModel: HoroscopeViewModel
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Screen1.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(BottomNavItem.Screen1.route) {
            // Llama a la pantalla mágica rediseñada
            Screen1Numbers(numbersViewModel = numbersViewModel, userDataViewModel = userDataViewModel)
        }
        composable(BottomNavItem.Screen2.route) {
            // Llama a la pantalla mágica rediseñada
            Screen2Numbers(numbersViewModel = numbersViewModel, userDataViewModel = userDataViewModel)
        }
        composable(BottomNavItem.Screen4.route) {
            // Llama a la pantalla mágica rediseñada
            HoroscopeScreen(userDataViewModel = userDataViewModel, horoscopeViewModel = horoscopeViewModel)
        }
        composable(BottomNavItem.Screen3.route) {
            // Asumiendo que Screen3Dreams será rediseñada de manera similar
            Screen3Dreams()
        }
    }
}



//package com.matancita.loteria.ui.theme.screen
//
//import androidx.compose.foundation.layout.Box
//// import androidx.compose.foundation.layout.PaddingValues // No es directamente necesario aquí
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Favorite // Sueños
//import androidx.compose.material.icons.filled.Looks3 // 3 Números
//import androidx.compose.material.icons.filled.Looks5 // 5 Números
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf // Ya está en runtime.*
//import androidx.compose.runtime.remember // Ya está en runtime.*
//import androidx.compose.runtime.setValue // Ya está en runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//// import androidx.compose.ui.platform.LocalContext // No es directamente necesario aquí
//import androidx.compose.ui.text.font.FontWeight // Para el texto de la barra de navegación
//import androidx.compose.ui.unit.sp // Para el tamaño de fuente del label
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.currentBackStackEntryAsState
//import androidx.navigation.compose.rememberNavController
//import com.matancita.loteria.ui.theme.GoldAccent
//import com.matancita.loteria.ui.theme.LuckyGreen
//import com.matancita.loteria.ui.theme.nav.AppDestinations
//import com.matancita.loteria.viewmodel.NumbersViewModel
//import com.matancita.loteria.viewmodel.UserDataViewModel
//
//// Suponiendo que Color.darken() está disponible (definida en otro lugar o aquí)
///*
//fun Color.darken(factor: Float): Color {
//    val invertedFactor = (1 - factor).coerceIn(0f, 1f)
//    return Color(
//        red = this.red * invertedFactor,
//        green = this.green * invertedFactor,
//        blue = this.blue * invertedFactor,
//        alpha = this.alpha
//    )
//}
//*/
//
//import androidx.annotation.StringRes
//import androidx.compose.ui.res.stringResource
//import com.matancita.loteria.R // Asegúrate de importar R
//import com.matancita.loteria.viewmodel.HoroscopeViewModel
//
//// ... otras importaciones
//
//sealed class BottomNavItem(
//    val route: String,
//    val icon: ImageVector,
//    @StringRes val labelResId: Int // Cambia 'label' por 'labelResId' y el tipo a Int
//) {
//    object Screen1 : BottomNavItem(
//        AppDestinations.SCREEN_1_NUMBERS,
//        Icons.Filled.Looks3,
//        R.string.tab_suerte // Usa el ID del recurso
//    )
//    object Screen2 : BottomNavItem(
//        AppDestinations.SCREEN_2_NUMBERS,
//        Icons.Filled.Looks5,
//        R.string.tab_chance // Usa el ID del recurso
//    )
//    object Screen3 : BottomNavItem(
//        AppDestinations.SCREEN_3_DREAMS,
//        Icons.Filled.Favorite,
//        R.string.tab_dreams// Usa el ID del recurso
//    )
//    object Screen4 : BottomNavItem(
//        AppDestinations.SCREEN_4_HOROSCOPE,
//        Icons.Filled.Favorite,
//        R.string.tab_horoscope // Usa el ID del recurso
//    )
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MainScreenLayout(
//    navController: NavController, // Para navegación general si es necesario (aunque aquí no se usa directamente)
//    userDataViewModel: UserDataViewModel = viewModel(),
//    numbersViewModel: NumbersViewModel = viewModel(),
//    horoscopeViewModel: HoroscopeViewModel = viewModel()
//) {
//    val bottomNavController = rememberNavController()
//    val userProfile by userDataViewModel.userProfile.collectAsState()
//
//    LaunchedEffect(userProfile) {
//        userProfile?.let {
//            numbersViewModel.loadNumbersForScreen("screen1", it)
//            numbersViewModel.loadNumbersForScreen("screen2", it)
//        }
//    }
//
//    Scaffold(
//        bottomBar = {
//            // Pasamos los colores principales del tema a la barra de navegación
//            BottomNavigationBar(
//                navController = bottomNavController,
//                containerColor = LuckyGreen.darken(0.2f), // MEJORA: Un verde más oscuro y profundo para la barra
//                selectedContentColor = GoldAccent,
//                unselectedContentColor = Color.White.copy(alpha = 0.75f), // MEJORA: Un poco más visible
//                indicatorColor = GoldAccent.copy(alpha = 0.15f) // MEJORA: Indicador dorado
//            )
//        }
//    ) { innerPadding ->
//        Box(modifier = Modifier.padding(innerPadding)) {
//            AppNavigationHost(
//                navController = bottomNavController,
//                userDataViewModel = userDataViewModel,
//                numbersViewModel = numbersViewModel,
//                horoscopeViewModel = horoscopeViewModel
//            )
//        }
//    }
//}
//
//@Composable
//fun BottomNavigationBar(
//    navController: NavHostController,
//    containerColor: Color,
//    selectedContentColor: Color,
//    unselectedContentColor: Color,
//    indicatorColor: Color
//) {
//    val items = listOf(
//        BottomNavItem.Screen1,
//        BottomNavItem.Screen2,
//        BottomNavItem.Screen4, // Nuevo item para el horóscopo
//        BottomNavItem.Screen3
//    )
//    // selectedItem y su LaunchedEffect para actualizarlo se eliminan,
//    // ya que currentRoute directamente determina el estado 'selected'.
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val currentRoute = navBackStackEntry?.destination?.route
//
//    NavigationBar(
//        containerColor = containerColor, // Usar el color pasado como parámetro
//        contentColor = unselectedContentColor // Color por defecto para el contenido no seleccionado
//    ) {
//        items.forEach { item ->
//            val isSelected = currentRoute == item.route
//            val labelText = stringResource(id = item.labelResId)
//            NavigationBarItem(
//                icon = { Icon(item.icon, contentDescription = labelText) },
//                label = { Text(labelText, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp) }, // MEJORA: Texto en negrita si está seleccionado y tamaño ajustado
//                selected = isSelected,
//                onClick = {
//                    if (currentRoute != item.route) {
//                        navController.navigate(item.route) {
//                            popUpTo(navController.graph.startDestinationId) {
//                                saveState = true
//                            }
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    }
//                },
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = selectedContentColor,
//                    selectedTextColor = selectedContentColor,
//                    unselectedIconColor = unselectedContentColor,
//                    unselectedTextColor = unselectedContentColor,
//                    indicatorColor = indicatorColor
//                ),
//                alwaysShowLabel = true // MEJORA: Mostrar siempre las etiquetas para claridad
//            )
//        }
//    }
//}
//
//@Composable
//fun AppNavigationHost(
//    navController: NavHostController,
//    userDataViewModel: UserDataViewModel,
//    numbersViewModel: NumbersViewModel,
//    horoscopeViewModel: HoroscopeViewModel
//) {
//    NavHost(
//        navController = navController,
//        startDestination = BottomNavItem.Screen1.route,
//        modifier = Modifier.fillMaxSize()
//    ) {
//        composable(BottomNavItem.Screen1.route) {
//            Screen1Numbers(numbersViewModel = numbersViewModel, userDataViewModel = userDataViewModel)
//        }
//        composable(BottomNavItem.Screen2.route) {
//            Screen2Numbers(numbersViewModel = numbersViewModel, userDataViewModel = userDataViewModel)
//        }
//        composable(BottomNavItem.Screen4.route) {
//            HoroscopeScreen(userDataViewModel = userDataViewModel, horoscopeViewModel = horoscopeViewModel)
//        }
//        composable(BottomNavItem.Screen3.route) {
//            Screen3Dreams()
//        }
//        // composable(AppDestinations.SETUP_SCREEN) { // Si SetupScreen es parte de este NavHost
//        // SetupScreen(userDataViewModel = userDataViewModel, onSetupComplete = { /* ... */ })
//        // }
//    }
//}