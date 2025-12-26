package com.ai.egret.ui.screens


import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ai.egret.R
import com.ai.egret.viewmodels.FieldRegistrationViewModel
import com.ai.egret.viewmodels.RegistrationState
import com.ai.egret.ui.theme.M3Theme


import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun FieldRegistrationScreen(
    navController: NavController,
    viewModel: FieldRegistrationViewModel = hiltViewModel()
) {
    // --- UI State ---
    var fieldName by remember { mutableStateOf("") }
    var cropName by remember { mutableStateOf("") }
    var cropDate by remember { mutableStateOf("") }
    var polygonPoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var isShapeFixed by remember { mutableStateOf(false) }

    // --- Map State ---
    val puneLatLng = LatLng(18.5204, 73.8567)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(puneLatLng, 10f)
    }
    var userLatLng by remember { mutableStateOf<LatLng?>(null) }
    var isLocationPermissionGranted by remember { mutableStateOf(false) }

    // --- Logic & Helpers ---
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val regState by viewModel.state.collectAsState()

    // Handle Registration State Changes (Success/Error)
    LaunchedEffect(regState) {
        when (val state = regState) {
            is RegistrationState.Success -> {
                Toast.makeText(context, context.getString(R.string.reg_success), Toast.LENGTH_LONG).show()
                navController.popBackStack() // Go back to Dashboard
                viewModel.resetState()
            }
            is RegistrationState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {} // Idle or Loading
        }
    }

    // Handle Location Permission
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status is PermissionStatus.Granted) {
            isLocationPermissionGranted = true
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                client.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        userLatLng = latLng
                        scope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    M3Theme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.reg_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Instruction
                Text(
                    stringResource(R.string.reg_draw_instruction),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 2. Permission Button (if needed)
                if (!isLocationPermissionGranted) {
                    OutlinedButton(
                        onClick = { locationPermissionState.launchPermissionRequest() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(stringResource(R.string.reg_permission_btn))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 3. Map Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .background(Color.LightGray, RoundedCornerShape(8.dp))
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = isLocationPermissionGranted,
                            mapType = MapType.HYBRID
                        ),
                        uiSettings = MapUiSettings(zoomControlsEnabled = true),
                        onMapClick = { latLng ->
                            if (!isShapeFixed) {
                                polygonPoints = polygonPoints + latLng
                            }
                        }
                    ) {
                        // Draw Markers
                        polygonPoints.forEachIndexed { idx, latLng ->
                            Marker(
                                state = MarkerState(position = latLng),
                                title = "P${idx + 1}"
                            )
                        }

                        // Draw Polygon
                        if (polygonPoints.size >= 2) {
                            Polygon(
                                points = polygonPoints,
                                strokeColor = MaterialTheme.colorScheme.primary,
                                fillColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                strokeWidth = 4f
                            )
                        }
                    }
                }

                // 4. Map Controls (Clear / Confirm)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Clear Button
                    OutlinedButton(
                        onClick = {
                            polygonPoints = emptyList()
                            isShapeFixed = false
                        },
                        enabled = polygonPoints.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.reg_clear))
                    }

                    // Confirm Button
                    Button(
                        onClick = { isShapeFixed = true },
                        enabled = polygonPoints.size >= 3 && !isShapeFixed
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.reg_confirm_shape))
                    }
                }

                // Helper Text
                val infoText = if (isShapeFixed) {
                    stringResource(R.string.msg_shape_fixed)
                } else {
                    stringResource(R.string.msg_points_selected, polygonPoints.size)
                }
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 5. Input Fields
                OutlinedTextField(
                    value = fieldName,
                    onValueChange = { fieldName = it },
                    label = { Text(stringResource(R.string.reg_field_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = { Icon(Icons.Default.Mic, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = cropName,
                    onValueChange = { cropName = it },
                    label = { Text(stringResource(R.string.reg_crop_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = { Icon(Icons.Default.Mic, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = cropDate,
                    onValueChange = { cropDate = it },
                    label = { Text(stringResource(R.string.sowing_crop_date)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = { Icon(Icons.Default.Mic, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 6. Save Button
                Button(
                    onClick = {
                        if (!isShapeFixed) {
                            Toast.makeText(context, context.getString(R.string.reg_error_shape), Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.registerFarm(fieldName, cropName, polygonPoints)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = regState !is RegistrationState.Loading
                ) {
                    if (regState is RegistrationState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.reg_saving))
                    } else {
                        Text(stringResource(R.string.reg_save_btn))
                    }
                }
            }
        }
    }
}