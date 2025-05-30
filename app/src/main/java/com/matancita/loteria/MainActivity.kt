package com.matancita.loteria // Asegúrate que tu paquete sea el correcto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator // Para el estado de carga
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle // ¡Importante!
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.matancita.loteria.ui.theme.nav.AppDestinations
import com.matancita.loteria.ui.theme.screen.MainScreenLayout
import com.matancita.loteria.ui.theme.screen.SetupScreen
import com.matancita.loteria.viewmodel.UserDataViewModel
// Asegúrate de tener tu LuckyNumbersAppTheme o el nombre que le hayas dado
import com.matancita.loteria.ui.theme.NumerosDeLaSuerteTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Asegúrate de usar el tema correcto de tu app.
            // Si lo llamaste LuckyNumbersAppTheme como en mi ejemplo anterior, úsalo.
            // Si es solo MaterialTheme, puede que no tengas los colores personalizados.
            NumerosDeLaSuerteTheme { // O el nombre de tu tema personalizado
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppEntry()
                }
            }
        }
    }
}

@Composable
fun AppEntry(userDataViewModel: UserDataViewModel = viewModel()) {
    val navController = rememberNavController()
    // Usar la función oficial collectAsStateWithLifecycle
    val isSetupComplete by userDataViewModel.isSetupComplete.collectAsStateWithLifecycle()

    // Determinar la ruta inicial basada en si el setup se completó
    val startDestination = when (isSetupComplete) {
        true -> AppDestinations.MAIN_APP_ROUTE
        false -> AppDestinations.SETUP_ROUTE
        null -> null // Estado de carga
    }

    if (startDestination != null) { // Solo navegamos cuando el destino está determinado
        NavHost(navController = navController, startDestination = startDestination) {
            composable(AppDestinations.SETUP_ROUTE) {
                SetupScreen(
                    userDataViewModel = userDataViewModel,
                    onSetupComplete = {
                        navController.navigate(AppDestinations.MAIN_APP_ROUTE) {
                            popUpTo(AppDestinations.SETUP_ROUTE) { inclusive = true }
                        }
                    }
                )
            }
            composable(AppDestinations.MAIN_APP_ROUTE) {
                // Pasa el navController general si es necesario para acciones de navegación
                // fuera del BottomNav, o el viewModel de userData si MainScreenLayout lo necesita.
                MainScreenLayout(
                    navController = navController, // Puede ser útil para un logout, etc.
                    userDataViewModel = userDataViewModel
                )
            }
        }
    } else {
        // Mostrar un indicador de carga mientras isSetupComplete es null
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
            // También puedes añadir un texto como "Cargando..."
        }
    }
}

// YA NO NECESITAS ESTA FUNCIÓN PERSONALIZADA:
// @Composable
// fun <T> collectAsStateWithLifecycleFixed(
//    flow: kotlinx.coroutines.flow.StateFlow<T>
// ): androidx.compose.runtime.State<T> {
//    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
//    return remember(flow, lifecycleOwner) {
//        flow.collectAsState(
//            initial = flow.value // Asegura valor inicial
//        )
//    }
// }