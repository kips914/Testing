package com.example.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.OliAppContainer
import com.example.R
import com.example.ui.about.AboutScreen
import com.example.ui.chat.ChatScreen
import com.example.ui.chat.ChatViewModel
import com.example.ui.connection.ConnectionScreen
import com.example.ui.connection.ConnectionViewModel
import com.example.ui.console.ConsoleScreen
import com.example.ui.console.ConsoleViewModel
import com.example.ui.diagnostics.DiagnosticsScreen
import com.example.ui.diagnostics.DiagnosticsViewModel
import com.example.ui.memory.MemoryScreen
import com.example.ui.memory.MemoryViewModel
import com.example.ui.ocr.OcrScreen
import com.example.ui.ocr.OcrViewModel
import com.example.ui.servers.ServersScreen
import com.example.ui.servers.ServersViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.splash.SplashScreen
import com.example.ui.theme.OliDarkBackground
import com.example.ui.theme.OliDarkSurface
import com.example.ui.theme.OliPrimary
import com.example.ui.theme.OliSecondary
import com.example.ui.theme.OliText

sealed class Screen(val route: String, val titleRes: Int, val icon: ImageVector) {
    object Splash : Screen("splash", R.string.splash_title, Icons.Default.Chat)
    object Chat : Screen("chat", R.string.nav_chat, Icons.Default.Chat)
    object Servers : Screen("servers", R.string.nav_servers, Icons.Default.Dns)
    object Memory : Screen("memory", R.string.nav_memory, Icons.Default.Psychology)
    object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Settings)
    object Connection : Screen("connection", R.string.nav_connection, Icons.Default.Dns)
    object Console : Screen("console", R.string.nav_console, Icons.Default.Settings)
    object Diagnostics : Screen("diagnostics", R.string.nav_diagnostics, Icons.Default.Settings)
    object Ocr : Screen("ocr", R.string.nav_ocr, Icons.Default.Chat)
    object About : Screen("about", R.string.nav_about, Icons.Default.Settings)
}

val BottomNavItems = listOf(
    Screen.Chat,
    Screen.Servers,
    Screen.Memory,
    Screen.Settings
)

@Composable
fun OliApp(
    container: OliAppContainer,
    initialDeepLink: String? = null,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ViewModels with constructor injection via container
    val chatViewModel = rememberViewModel { ChatViewModel(container.chatRepository, container.connectionRepository) }
    val serversViewModel = rememberViewModel { ServersViewModel(container.connectionRepository, container.serverManager) }
    val connectionViewModel = rememberViewModel { ConnectionViewModel(container.connectionRepository) }
    val settingsViewModel = rememberViewModel { SettingsViewModel(container.settingsRepository, container.connectionRepository, container.tailscaleTransport) }
    val memoryViewModel = rememberViewModel { MemoryViewModel(container.memoryRepository) }
    val consoleViewModel = rememberViewModel { ConsoleViewModel(container.serverManager) }
    val diagnosticsViewModel = rememberViewModel { DiagnosticsViewModel(container.serverManager) }
    val ocrViewModel = rememberViewModel { OcrViewModel() }

    // Handle deep link if present
    androidx.compose.runtime.LaunchedEffect(initialDeepLink) {
        if (!initialDeepLink.isNullOrBlank()) {
            connectionViewModel.handleScannedQr(initialDeepLink)
            navController.navigate(Screen.Connection.route)
        }
    }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val showNavBars = currentRoute in BottomNavItems.map { it.route }

    if (isTablet && showNavBars) {
        // Adaptive layout: NavigationRail on tablets
        Row(modifier = Modifier.fillMaxSize().background(OliDarkBackground)) {
            NavigationRail(
                containerColor = OliDarkSurface,
                modifier = Modifier.fillMaxHeight()
            ) {
                BottomNavItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationRailItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = stringResource(screen.titleRes), modifier = Modifier.size(22.dp)) },
                        label = { Text(stringResource(screen.titleRes), fontSize = 11.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = OliPrimary,
                            selectedTextColor = OliPrimary,
                            unselectedIconColor = OliSecondary,
                            unselectedTextColor = OliSecondary,
                            indicatorColor = Color(0xFF1E2846)
                        )
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                OliNavHost(
                    navController = navController,
                    chatViewModel = chatViewModel,
                    serversViewModel = serversViewModel,
                    connectionViewModel = connectionViewModel,
                    settingsViewModel = settingsViewModel,
                    memoryViewModel = memoryViewModel,
                    consoleViewModel = consoleViewModel,
                    diagnosticsViewModel = diagnosticsViewModel,
                    ocrViewModel = ocrViewModel
                )
            }
        }
    } else {
        // Mobile layout: Bottom NavigationBar on handhelds
        Scaffold(
            bottomBar = {
                if (showNavBars) {
                    NavigationBar(
                        containerColor = OliDarkSurface
                    ) {
                        BottomNavItems.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(screen.icon, contentDescription = stringResource(screen.titleRes), modifier = Modifier.size(22.dp)) },
                                label = { Text(stringResource(screen.titleRes), fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = OliPrimary,
                                    selectedTextColor = OliPrimary,
                                    unselectedIconColor = OliSecondary,
                                    unselectedTextColor = OliSecondary,
                                    indicatorColor = Color(0xFF1E2846)
                                )
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                OliNavHost(
                    navController = navController,
                    chatViewModel = chatViewModel,
                    serversViewModel = serversViewModel,
                    connectionViewModel = connectionViewModel,
                    settingsViewModel = settingsViewModel,
                    memoryViewModel = memoryViewModel,
                    consoleViewModel = consoleViewModel,
                    diagnosticsViewModel = diagnosticsViewModel,
                    ocrViewModel = ocrViewModel
                )
            }
        }
    }
}

@Composable
fun OliNavHost(
    navController: NavHostController,
    chatViewModel: ChatViewModel,
    serversViewModel: ServersViewModel,
    connectionViewModel: ConnectionViewModel,
    settingsViewModel: SettingsViewModel,
    memoryViewModel: MemoryViewModel,
    consoleViewModel: ConsoleViewModel,
    diagnosticsViewModel: DiagnosticsViewModel,
    ocrViewModel: OcrViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen(
                viewModel = chatViewModel,
                onNavigateToOcr = { navController.navigate(Screen.Ocr.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Servers.route) {
            ServersScreen(
                viewModel = serversViewModel,
                onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                onNavigateToConnection = { navController.navigate(Screen.Connection.route) }
            )
        }

        composable(Screen.Memory.route) {
            MemoryScreen(viewModel = memoryViewModel)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateToConnection = { navController.navigate(Screen.Connection.route) },
                onNavigateToConsole = { navController.navigate(Screen.Console.route) },
                onNavigateToDiagnostics = { navController.navigate(Screen.Diagnostics.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.Connection.route) {
            ConnectionScreen(
                viewModel = connectionViewModel,
                onConnected = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Connection.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Console.route) {
            ConsoleScreen(
                viewModel = consoleViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Diagnostics.route) {
            DiagnosticsScreen(
                viewModel = diagnosticsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToConnection = { navController.navigate(Screen.Connection.route) }
            )
        }

        composable(Screen.Ocr.route) {
            OcrScreen(
                viewModel = ocrViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSendToChat = { recognizedText ->
                    chatViewModel.sendMessage("OCR Recognized Document:\n\n$recognizedText")
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Ocr.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

@Composable
inline fun <reified VM : androidx.lifecycle.ViewModel> rememberViewModel(
    crossinline factory: () -> VM
): VM {
    val owner = androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner.current
        ?: throw IllegalStateException("No ViewModelStoreOwner was provided via LocalViewModelStoreOwner")
    return androidx.lifecycle.viewmodel.compose.viewModel(
        viewModelStoreOwner = owner,
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return factory() as T
            }
        }
    )
}
