package com.ai.egret.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- 1. YOUR NEW LIME GREEN PALETTE ---

// The exact color you requested (#87e03a)
val HarvestNeonGreen = Color(0xFF87E03A)

// A "Milky" background that matches the new Lime Green (Very pale yellow-green)
val HarvestPaleLimeContainer = Color(0xFFF4FDE8)

// Text Colors
// IMPORTANT: Bright lime needs DARK text to look vibrant.
// If you use white text, the button looks blurry/washed out.
val OnHarvestNeon = Color(0xFF1A380F) // Dark Forest Green text on Neon Buttons
val OnHarvestPaleContainer = Color(0xFF1A380F)

val HarvestGoldTertiary = Color(0xFFFFC107)

private val DarkColorScheme = darkColorScheme(
    primary = HarvestNeonGreen,
    onPrimary = OnHarvestNeon, // Dark text on the bright button

    primaryContainer = Color(0xFF2F5018), // Dark organic green for containers
    onPrimaryContainer = HarvestPaleLimeContainer, // Light text

    secondary = HarvestNeonGreen,
    onSecondary = OnHarvestNeon,
    secondaryContainer = Color(0xFF2F5018),
    onSecondaryContainer = HarvestPaleLimeContainer,

    tertiary = HarvestGoldTertiary,

    background = Color(0xFF121611), // Deep dark green-black
    surface = Color(0xFF1A1F19)
)

private val LightColorScheme = lightColorScheme(
    primary = HarvestNeonGreen,
    onPrimary = OnHarvestNeon, // <--- CHANGED TO DARK TEXT (Makes the Green pop!)

    primaryContainer = HarvestPaleLimeContainer,
    onPrimaryContainer = OnHarvestPaleContainer,

    secondary = HarvestNeonGreen,
    onSecondary = OnHarvestNeon,

    secondaryContainer = HarvestPaleLimeContainer,
    onSecondaryContainer = OnHarvestPaleContainer,

    tertiary = HarvestGoldTertiary,

    // A very subtle, warm lime-tinted white for background
    background = Color(0xFFFCFEFA),
    surface = Color.White
)

@Composable
fun M3Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // FALSE to force your colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // --- SYSTEM BAR FIX ---
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}