package com.ai.egret.ui.navigation


import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.ai.egret.ui.screens.CropHealthDetailScreen
import com.ai.egret.ui.screens.CropHealthScreen
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
import com.ai.egret.viewmodels.CropHealthViewModel
import com.ai.egret.ui.screens.CropHealthHistoryScreen
import com.ai.egret.ui.screens.SoilAnalysisResultScreen
import com.ai.egret.ui.screens.SoilAnalysisScreen
import com.ai.egret.viewmodels.CropHealthHistoryViewModel
import com.ai.egret.ui.screens.SoilAnalysisHistoryScreen

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
                    LoginScreen(navController)
                }



                composable(
                    route = "crop_health/{farmId}",
                    arguments = listOf(navArgument("farmId") { type = NavType.IntType })
                ) {
                    val farmId = it.arguments?.getInt("farmId") ?: return@composable
                    CropHealthScreen(navController, farmId)
                }



                composable("crop_health_detail") {
                    CropHealthDetailScreen(navController)
                }
                composable(
                    route = "crop_health_detail/{analysisId}",
                    arguments = listOf(navArgument("analysisId") { type = NavType.IntType })
                ) {
                    CropHealthDetailScreen(navController)
                }



                composable("crop_health_history") {
                    val vm: CropHealthHistoryViewModel = hiltViewModel()
                    LaunchedEffect(Unit) {
                        vm.load()
                    }
                    CropHealthHistoryScreen(navController, vm)
                }


                composable("dashboard") {
                    FarmerDashboard(navController)
                }

                composable("market_prices") {
                    MarketPriceScreen(navController)
                }

                composable("my_farms") {
                    My_Farms(navController)
                }

                composable("weather_forecast") {
                    val vm: WeatherViewModel = hiltViewModel()

                    LaunchedEffect(Unit) {
                        vm.loadForLocation(17.3850, 78.4867)
                    }

                    WeatherScreen(navController, vm)
                }

                composable("register_field") {
                    FieldRegistrationScreen(navController)
                }


                composable(
                    route = "soil_analysis/{farmId}",
                    arguments = listOf(navArgument("farmId") { type = NavType.IntType })
                ) {
                    val farmId = it.arguments?.getInt("farmId") ?: return@composable
                    SoilAnalysisScreen(navController, farmId)
                }


                composable(
                    route = "soil_analysis_result/{reportId}",
                    arguments = listOf(navArgument("reportId") { type = NavType.IntType })
                ) {
                    SoilAnalysisResultScreen(navController)
                }


                composable("soil_analysis_history") {
                    SoilAnalysisHistoryScreen(navController)
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

