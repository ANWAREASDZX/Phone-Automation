package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AuditScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.PhoneAutomationTheme
import com.example.ui.theme.ThemeAccent
import com.example.ui.theme.ThemeBg
import com.example.ui.theme.ThemeBorder
import com.example.ui.theme.ThemeGlassBorder
import com.example.ui.theme.ThemeMuted
import com.example.ui.theme.ThemeSurface
import com.example.viewmodel.AgentViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: AgentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PhoneAutomationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val currentScreen by viewModel.currentScreen.collectAsState()

                    // BackHandler for sub-screens
                    BackHandler(enabled = currentScreen != Screen.HOME) {
                        viewModel.navigateTo(Screen.HOME)
                    }

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ThemeBg),
                        contentWindowInsets = WindowInsets.safeDrawing,
                        bottomBar = {
                            if (currentScreen != Screen.ONBOARDING) {
                                AppBottomNavigationBar(
                                    currentScreen = currentScreen,
                                    onSelectScreen = { viewModel.navigateTo(it) }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ThemeBg)
                                .padding(innerPadding)
                        ) {
                            Crossfade(
                                targetState = currentScreen,
                                label = "screen_transition"
                            ) { screen ->
                                when (screen) {
                                    Screen.ONBOARDING -> OnboardingScreen(
                                        onComplete = { viewModel.navigateTo(Screen.HOME) }
                                    )
                                    Screen.HOME -> HomeScreen(
                                        viewModel = viewModel,
                                        onNavigateToStudio = { viewModel.navigateTo(Screen.STUDIO) },
                                        onNavigateToMemory = { viewModel.navigateTo(Screen.MEMORY) }
                                    )
                                    Screen.STUDIO -> StudioScreen(viewModel = viewModel)
                                    Screen.MEMORY -> MemoryScreen(viewModel = viewModel)
                                    Screen.AUDIT -> AuditScreen(viewModel = viewModel)
                                    Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppBottomNavigationBar(
    currentScreen: Screen,
    onSelectScreen: (Screen) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .navigationBarsPadding()
            .border(width = 0.5.dp, color = ThemeGlassBorder),
        containerColor = ThemeSurface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.HOME,
            onClick = { onSelectScreen(Screen.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
            label = { Text("الرئيسية", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ThemeBg,
                selectedTextColor = ThemeAccent,
                indicatorColor = ThemeAccent,
                unselectedIconColor = ThemeMuted,
                unselectedTextColor = ThemeMuted
            ),
            modifier = Modifier.testTag("nav_home")
        )

        NavigationBarItem(
            selected = currentScreen == Screen.STUDIO,
            onClick = { onSelectScreen(Screen.STUDIO) },
            icon = { Icon(Icons.Default.Tune, contentDescription = "الاستوديو") },
            label = { Text("الاستوديو", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ThemeBg,
                selectedTextColor = ThemeAccent,
                indicatorColor = ThemeAccent,
                unselectedIconColor = ThemeMuted,
                unselectedTextColor = ThemeMuted
            ),
            modifier = Modifier.testTag("nav_studio")
        )

        NavigationBarItem(
            selected = currentScreen == Screen.MEMORY,
            onClick = { onSelectScreen(Screen.MEMORY) },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "الذاكرة") },
            label = { Text("الذاكرة", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ThemeBg,
                selectedTextColor = ThemeAccent,
                indicatorColor = ThemeAccent,
                unselectedIconColor = ThemeMuted,
                unselectedTextColor = ThemeMuted
            ),
            modifier = Modifier.testTag("nav_memory")
        )

        NavigationBarItem(
            selected = currentScreen == Screen.AUDIT,
            onClick = { onSelectScreen(Screen.AUDIT) },
            icon = { Icon(Icons.Default.History, contentDescription = "السجل") },
            label = { Text("السجل", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ThemeBg,
                selectedTextColor = ThemeAccent,
                indicatorColor = ThemeAccent,
                unselectedIconColor = ThemeMuted,
                unselectedTextColor = ThemeMuted
            ),
            modifier = Modifier.testTag("nav_audit")
        )

        NavigationBarItem(
            selected = currentScreen == Screen.SETTINGS,
            onClick = { onSelectScreen(Screen.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "الإعدادات") },
            label = { Text("الإعدادات", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ThemeBg,
                selectedTextColor = ThemeAccent,
                indicatorColor = ThemeAccent,
                unselectedIconColor = ThemeMuted,
                unselectedTextColor = ThemeMuted
            ),
            modifier = Modifier.testTag("nav_settings")
        )
    }
}
