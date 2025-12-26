package com.ai.egret




import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api


import androidx.core.view.WindowCompat
import com.ai.egret.ui.navigation.AppNavigator
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.FirebaseApp

@AndroidEntryPoint
@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            android.util.Log.e("EgretApp", "Firebase Init Failed", e)
        }

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("UncaughtException", "App crashed", throwable)
        }

        setContent {
            AppNavigator()
        }



    }

}



