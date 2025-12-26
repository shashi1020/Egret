package com.ai.egret.ui.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.ai.egret.R // Make sure this matches your R file

// 1. Data Class (Updated to use Resource ID for multi-language)
data class BottomNavItem(
    val route: String,
    val labelResId: Int, // Changed from String to Int (R.string.xxx)
    val icon: @Composable () -> Unit
)

// 2. The List of Tabs
// Note: Ensure you add these strings to your res/values/strings.xml
val appBottomNavItems = listOf(
    BottomNavItem(
        route = "dashboard",
        labelResId = R.string.nav_satellite, // e.g., "Satellite" or "सॅटेलाइट"
        icon = { Icon(Icons.Default.Crop, contentDescription = null) }
    ),
    BottomNavItem(
        route = "weather_forecast",
        labelResId = R.string.nav_weather,   // e.g., "Weather" or "हवामान"
        icon = { Icon(Icons.Default.Cloud, contentDescription = null) }
    ),
    BottomNavItem(
        route = "market_prices",
        labelResId = R.string.nav_market,    // e.g., "Market" or "बाजारभाव"
        icon = { Icon(Icons.Default.ShowChart, contentDescription = null) }
    )
)

// 3. Routes that show the bottom bar
val bottomBarRoutes = setOf(
    "dashboard",
    "weather_forecast",
    "market_prices"
)