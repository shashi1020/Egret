package com.ai.egret.ui.navigation


import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // <--- THE MAGIC IMPORT
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.ai.egret.ui.screens.FarmInsightsScreen
import com.ai.egret.ui.screens.FarmerDashboard
import com.ai.egret.ui.screens.FieldRegistrationScreen
import com.ai.egret.ui.screens.LoginScreen
import com.ai.egret.ui.screens.MarketPriceScreen
import com.ai.egret.ui.screens.My_Farms
import com.ai.egret.ui.screens.NotificationsScreen
import com.ai.egret.ui.screens.SettingsScreen
import com.ai.egret.ui.theme.M3Theme
import com.ai.egret.viewmodels.WeatherViewModel
import com.google.firebase.auth.FirebaseAuth
import com.ai.egret.ui.screens.WeatherScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigator() {

    val navController = rememberNavController()
    // Optional: You could even move this Auth check to a SplashViewModel!
    val context = androidx.compose.ui.platform.LocalContext.current

    val isUserLoggedIn = remember {
        try {
            // Check if Firebase is actually ready using the captured context
            if (com.google.firebase.FirebaseApp.getApps(context).isNotEmpty()) {
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("AppNavigator", "Auth check failed", e)
            false
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    M3Theme {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            containerColor = Color.Transparent,
            bottomBar = {
                if (currentRoute in bottomBarRoutes) {
                    BottomNavigationBar(
                        navController = navController,
                        items = appBottomNavItems
                    )
                }
            }
        ) { innerPadding ->

            NavHost(
                navController = navController,
                startDestination = if (isUserLoggedIn) "dashboard" else "login",
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            ) {



                composable("login") {
                    // CLEAN: No need to pass ApiService anymore.
                    // LoginScreen will use hiltViewModel() internally.
                    LoginScreen(navController)
                }


                composable("dashboard") {
                    FarmerDashboard(navController)
                }

                composable("market_prices") {
                    // CLEAN: ViewModel is injected inside the screen
                    MarketPriceScreen(navController)
                }

                composable("my_farms") {
                    My_Farms(navController)
                }

                composable("weather_forecast") {
                    // MAGIC: Hilt automatically builds the VM with the Repo & API Key
                    val vm: WeatherViewModel = hiltViewModel()

                    // Trigger initial load
                    LaunchedEffect(Unit) {
                        vm.loadForLocation(17.3850, 78.4867)
                    }

                    WeatherScreen(navController, vm)
                }

                composable("register_field") {
                    FieldRegistrationScreen(navController)
                }

                composable("notifications") {
                    NotificationsScreen()
                }

                composable("settings") {
                    SettingsScreen()
                }

                composable(
                    route = "farm_insights/{farmId}",
                    arguments = listOf(
                        navArgument("farmId") { type = NavType.IntType }
                    )
                ) {
                    val farmId = it.arguments?.getInt("farmId") ?: return@composable
                    // Hilt will inject the VM inside this screen too
                    FarmInsightsScreen(navController, farmId)
                }
            }
        }
    }
}

