package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.AppViewModel
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CompoundScreen
import com.example.ui.screens.ExecutionScreen
import com.example.ui.screens.PlansScreen
import com.example.ui.theme.CardBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup continuous ViewModel
        viewModel = ViewModelProvider(this)[AppViewModel::class.java]
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

sealed class NavigationTab(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object Compound : NavigationTab(
        route = "compound",
        title = "Compound",
        icon = { Icon(Icons.Default.Build, contentDescription = "Calculations") }
    )
    object Execution : NavigationTab(
        route = "execution",
        title = "Jurnal",
        icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Instant lot calculator") }
    )
    object Analytics : NavigationTab(
        route = "analytics",
        title = "Analytics",
        icon = { Icon(Icons.Default.Info, contentDescription = "Analytics stats") }
    )
    object Plans : NavigationTab(
        route = "plans",
        title = "Plans",
        icon = { Icon(Icons.Default.List, contentDescription = "Local strategy plans") }
    )
}

@Composable
fun MainAppContainer(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavigationTab.Compound.route
    val haptic = LocalHapticFeedback.current

    val tabsList = listOf(
        NavigationTab.Compound,
        NavigationTab.Execution,
        NavigationTab.Analytics,
        NavigationTab.Plans
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        bottomBar = {
            // Curvaceous Floating Curved Bottom Navigation Bar inside 16.dp margin padding
            NavigationBar(
                containerColor = CardBackground,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .navigationBarsPadding()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(24.dp))
            ) {
                tabsList.forEach { tab ->
                    val isSelected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = tab.icon,
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonGreen,
                            selectedTextColor = NeonGreen,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = ElectricBlue.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            NavHost(
                navController = navController,
                startDestination = NavigationTab.Compound.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(NavigationTab.Compound.route) {
                    CompoundScreen(viewModel = viewModel)
                }
                composable(NavigationTab.Execution.route) {
                    ExecutionScreen(viewModel = viewModel)
                }
                composable(NavigationTab.Analytics.route) {
                    AnalyticsScreen(viewModel = viewModel)
                }
                composable(NavigationTab.Plans.route) {
                    PlansScreen(
                        viewModel = viewModel,
                        onPlanLoaded = {
                            navController.navigate(NavigationTab.Compound.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}
