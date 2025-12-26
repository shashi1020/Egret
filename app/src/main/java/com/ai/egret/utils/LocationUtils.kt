package com.ai.egret.utils


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// --- 1. Helper Function: Reverse Geocode (Coordinates -> State/District) ---
// Note: This blocks the thread, so we call it inside Dispatchers.IO below.
fun reverseGeocode(context: Context, location: Location): Pair<String?, String?> {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        // API 33+ has a listener-based getFromLocation, but this deprecated version
        // is simpler for now and works fine if run on a background thread.
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            // Prefer locality (City) or subAdminArea (District)
            val district = address.subAdminArea ?: address.locality ?: address.subLocality
            val state = address.adminArea // State is usually accurate
            Pair(state, district)
        } else {
            Pair(null, null)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Pair(null, null)
    }
}

// --- 2. Composable: Request Permission & Fetch Location ---
@Composable
fun RequestAndFetchLocation(
    onLocationResult: (latitude: Double, longitude: Double, state: String?, district: String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Helper to check if GPS is on
    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            if (!isLocationEnabled()) {
                Toast.makeText(context, "Please turn on GPS", Toast.LENGTH_LONG).show()
                try {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return@rememberLauncherForActivityResult
            }

            // Permission Granted: Get Location
            try {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

                    fusedLocationProviderClient.lastLocation
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                // CRITICAL FIX: Run Geocoding on Background Thread (IO)
                                // otherwise, the app will freeze or crash on Main Thread.
                                scope.launch(Dispatchers.IO) {
                                    val (state, district) = reverseGeocode(context, location)

                                    // Switch back to Main Thread to update UI
                                    withContext(Dispatchers.Main) {
                                        onLocationResult(location.latitude, location.longitude, state, district)
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                        }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Trigger request when Composable enters composition
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}