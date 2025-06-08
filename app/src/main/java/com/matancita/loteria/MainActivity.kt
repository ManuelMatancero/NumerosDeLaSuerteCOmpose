package com.matancita.loteria

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.matancita.loteria.internet.ConnectivityObserver
import com.matancita.loteria.ui.theme.NumerosDeLaSuerteTheme
import com.matancita.loteria.ui.theme.nav.AppDestinations
import com.matancita.loteria.ui.theme.screen.MainScreenLayout
import com.matancita.loteria.ui.theme.screen.SetupScreen
import com.matancita.loteria.viewmodel.UserDataViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NumerosDeLaSuerteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // El nuevo punto de entrada que primero verifica la conexión.
                    AppEntryPoint()
                }
            }
        }
    }
}

@Composable
fun AppEntryPoint() {
    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }
    // Observamos el estado de la conexión. Valor inicial es `true`.
    val hasConnection by connectivityObserver.observe().collectAsState(initial = true)

    // --- LÓGICA PARA SOLICITAR PERMISO DE NOTIFICACIÓN ---
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                // Opcional: Manejar si el usuario deniega el permiso
            }
        )

        LaunchedEffect(key1 = true) {
            val permission = "android.permission.POST_NOTIFICATIONS" // Usar la cadena de texto
            val permissionStatus = ContextCompat.checkSelfPermission(context, permission)
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(permission) // Usar la cadena de texto
            }
        }
    }
    // --- FIN DE LA LÓGICA DE PERMISOS ---

    if (hasConnection) {
        // Si hay conexión, procedemos con la lógica normal de la app.
        AppNavigation()
    } else {
        // Si no hay conexión, mostramos la pantalla de error.
        NoInternetScreen()
    }
}

@Composable
fun AppNavigation(userDataViewModel: UserDataViewModel = viewModel()) {
    val navController = rememberNavController()
    val isSetupComplete by userDataViewModel.isSetupComplete.collectAsStateWithLifecycle()

    val startDestination = when (isSetupComplete) {
        true -> AppDestinations.MAIN_APP_ROUTE
        false -> AppDestinations.SETUP_ROUTE
        null -> null // Estado de carga
    }

    if (startDestination != null) {
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
                MainScreenLayout(
                    navController = navController,
                    userDataViewModel = userDataViewModel
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun NoInternetScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = "Sin Conexión",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Sin Conexión a Internet",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Por favor, revisa tu conexión para poder usar la aplicación.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}