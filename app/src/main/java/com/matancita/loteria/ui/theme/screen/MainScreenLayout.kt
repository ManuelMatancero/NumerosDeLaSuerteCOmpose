package com.matancita.loteria.ui.theme.screen

import android.app.DatePickerDialog
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import com.matancita.loteria.repository.UserProfile
import com.matancita.loteria.ui.theme.nav.AppDestinations
import com.matancita.loteria.viewmodel.HoroscopeViewModel
import com.matancita.loteria.viewmodel.LotteryGamesViewModel
import com.matancita.loteria.viewmodel.NumbersViewModel
import com.matancita.loteria.viewmodel.OracleViewModel
import com.matancita.loteria.viewmodel.UserDataViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- Modelos de Navegación y Datos ---
sealed class NavItem(
    val route: String,
    val icon: ImageVector,
    @StringRes val titleResId: Int
) {
    object Screen1 : NavItem(AppDestinations.SCREEN_1_NUMBERS, Icons.Filled.Looks3, R.string.tab_suerte)
    object Screen2 : NavItem(AppDestinations.SCREEN_2_NUMBERS, Icons.Filled.Looks5, R.string.tab_chance)
    object Screen6 : NavItem(AppDestinations.SCREEN_6_LOTTERY, Icons.Default.Casino, R.string.tab_lottery)
    object Screen4 : NavItem(AppDestinations.SCREEN_4_HOROSCOPE, Icons.Default.AutoAwesome, R.string.tab_horoscope)
    object Screen5 : NavItem(AppDestinations.SCREEN_5_LUCKY_SEARCH, Icons.Filled.Search, R.string.tab_lucky_search)
    object Screen3 : NavItem(AppDestinations.SCREEN_3_DREAMS, Icons.Filled.Bed, R.string.tab_dreams)
    object Screen7 : NavItem(AppDestinations.SCREEN_7_HELP, Icons.AutoMirrored.Filled.HelpOutline, R.string.tab_help)
    object Screen8 : NavItem(AppDestinations.SCREEN_8_ORACLE, Icons.Default.TouchApp, R.string.tab_oracle)
}

// --- Layout Principal con Menú Lateral ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenLayout(
    navController: NavController,
    userDataViewModel: UserDataViewModel = viewModel(),
    numbersViewModel: NumbersViewModel = viewModel(),
    horoscopeViewModel: HoroscopeViewModel = viewModel(),
    lotteryGamesViewModel: LotteryGamesViewModel = viewModel(),
    oracleViewModel: OracleViewModel = viewModel()
) {
    val drawerNavController = rememberNavController()
    val userProfile by userDataViewModel.userProfile.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showEditSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val navItems = listOf(
        NavItem.Screen1,
        NavItem.Screen2,
        NavItem.Screen6,
        NavItem.Screen8,
        NavItem.Screen4,
        NavItem.Screen5,
        NavItem.Screen3
    )
    val helpItem = NavItem.Screen7

    LaunchedEffect(userProfile) {
        userProfile?.let {
            numbersViewModel.loadNumbersForScreen("screen1", it)
            numbersViewModel.loadNumbersForScreen("screen2", it)
        }
    }

    if (showEditSheet) {
        EditProfileSheet(
            userProfile = userProfile,
            sheetState = sheetState,
            onDismiss = { showEditSheet = false },
            onSave = { name, dob ->
                userDataViewModel.saveUserProfile(name, dob)
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showEditSheet = false
                    }
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF0c0c2b).copy(alpha = 0.95f)
            ) {
                DrawerHeader(userProfile = userProfile)
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)

                Column(modifier = Modifier.weight(1f)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    navItems.forEach { item ->
                        DrawerItem(navController = drawerNavController, item = item, scope = scope, drawerState = drawerState)
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                DrawerItem(navController = drawerNavController, item = helpItem, scope = scope, drawerState = drawerState)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                val navBackStackEntry by drawerNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val allItems = navItems + helpItem
                val currentScreen = allItems.find { it.route == currentRoute }

                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = currentScreen?.let { stringResource(id = it.titleResId) } ?: "",
                            fontFamily = FontFamily.Serif
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.drawer_open_menu))
                        }
                    },
                    actions = {
                        IconButton(onClick = { showEditSheet = true }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.drawer_edit_profile))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF0c0c2b).copy(alpha = 0.85f),
                        titleContentColor = Color.White.copy(alpha = 0.9f),
                        navigationIconContentColor = Color.White.copy(alpha = 0.9f),
                        actionIconContentColor = Color.White.copy(alpha = 0.9f)
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                MagicalAppNavigationHost(
                    navController = drawerNavController,
                    userDataViewModel = userDataViewModel,
                    numbersViewModel = numbersViewModel,
                    horoscopeViewModel = horoscopeViewModel,
                    lotteryGamesViewModel = lotteryGamesViewModel,
                    oracleViewModel = oracleViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSheet(
    userProfile: UserProfile?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit
) {
    var name by remember(userProfile?.name) { mutableStateOf(userProfile?.name ?: "") }
    var dobTimestamp by remember(userProfile?.dob) { mutableStateOf(userProfile?.dob) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    dobTimestamp?.let { calendar.timeInMillis = it }

    val dateFormatter = remember { SimpleDateFormat("dd / MM / yyyy", Locale.getDefault()) }
    val selectedDateText = dobTimestamp?.let { dateFormatter.format(Date(it)) } ?: stringResource(id = R.string.setup_dob_placeholder)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            dobTimestamp = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0c0c2b).copy(alpha = 0.98f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.edit_profile_title), fontSize = 22.sp, color = Color.White, fontFamily = FontFamily.Serif)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.setup_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.8f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done)
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
            ) {
                Row(modifier = Modifier.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                    Spacer(Modifier.width(8.dp))
                    Text(selectedDateText, color = Color.White.copy(alpha = 0.9f))
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { dobTimestamp?.let { onSave(name.trim(), it) } },
                enabled = name.isNotBlank() && dobTimestamp != null,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Text(stringResource(R.string.common_save))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerItem(
    navController: NavHostController,
    item: NavItem,
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isSelected = currentRoute == item.route

    NavigationDrawerItem(
        icon = { Icon(item.icon, contentDescription = stringResource(id = item.titleResId)) },
        label = { Text(text = stringResource(id = item.titleResId)) },
        selected = isSelected,
        onClick = {
            if (currentRoute != item.route) {
                navController.navigate(item.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            scope.launch {
                drawerState.close()
            }
        },
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            selectedContainerColor = Color.White.copy(alpha = 0.1f),
            selectedIconColor = Color.White,
            unselectedIconColor = Color.White.copy(alpha = 0.7f),
            selectedTextColor = Color.White,
            unselectedTextColor = Color.White.copy(alpha = 0.7f)
        ),
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Composable
fun DrawerHeader(userProfile: UserProfile?) {
    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "User Icon",
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            val name = userProfile?.name?.takeIf { it.isNotBlank() } ?: stringResource(id = R.string.drawer_header_greeting)
            Text(
                text = stringResource(id = R.string.drawer_hello, name),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            Spacer(modifier = Modifier.height(4.dp))

            val dob = userProfile?.dob?.let { dateFormatter.format(Date(it)) }
            val zodiac = userProfile?.zodiacSign?.takeIf { it.isNotBlank() }

            val details = when {
                dob != null && zodiac != null -> "$dob  •  $zodiac"
                dob != null -> dob
                zodiac != null -> zodiac
                else -> null
            }

            details?.let {
                Text(
                    text = it,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun MagicalAppNavigationHost(
    navController: NavHostController,
    userDataViewModel: UserDataViewModel,
    numbersViewModel: NumbersViewModel,
    horoscopeViewModel: HoroscopeViewModel,
    lotteryGamesViewModel: LotteryGamesViewModel,
    oracleViewModel: OracleViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NavItem.Screen1.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(NavItem.Screen1.route) {
            Screen1Numbers(numbersViewModel = numbersViewModel, userDataViewModel = userDataViewModel)
        }
        composable(NavItem.Screen2.route) {
            Screen2Numbers(numbersViewModel = numbersViewModel, userDataViewModel = userDataViewModel)
        }
        composable(NavItem.Screen6.route) {
            LotteryGamesScreen(viewModel = lotteryGamesViewModel)
        }
        composable(NavItem.Screen8.route) {
            OracleOfTimeScreen(viewModel = oracleViewModel) // Necesitarás un oracleViewModel
        }
        composable(NavItem.Screen4.route) {
            HoroscopeScreen(userDataViewModel = userDataViewModel, horoscopeViewModel = horoscopeViewModel)
        }
        composable(NavItem.Screen5.route) {
            FindYourLuckScreen()
        }
        composable(NavItem.Screen3.route) {
            Screen3Dreams()
        }
        composable(NavItem.Screen7.route) {
            HelpScreen()
        }
    }
}
