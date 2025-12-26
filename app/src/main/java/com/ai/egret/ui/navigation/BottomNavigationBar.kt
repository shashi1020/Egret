package com.ai.egret.ui.navigation


import android.util.Log
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource // Import this
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController, items: List<BottomNavItem>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = item.icon,
                // FIX: Use stringResource to support English/Marathi switching
                label = { Text(stringResource(id = item.labelResId)) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute == item.route) return@NavigationBarItem

                    try {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                            // Safe popUpTo logic
                            navController.graph.startDestinationRoute?.let { startRoute ->
                                popUpTo(startRoute) {
                                    saveState = true
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("BottomNav", "Navigation error: ${e.message}")
                    }
                }
            )
        }
    }
}