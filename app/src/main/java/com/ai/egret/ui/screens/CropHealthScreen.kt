//package com.ai.egret.ui.screens
//
//import android.net.Uri
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.PickVisualMediaRequest
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.AddPhotoAlternate
//import androidx.compose.material.icons.filled.History
//import androidx.compose.material.icons.outlined.CloudUpload
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavController
//import coil.compose.AsyncImage
//import com.ai.egret.viewmodels.CropHealthViewModel
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CropHealthScreen(
//    navController: NavController,
//    viewModel: CropHealthViewModel = hiltViewModel()
//) {
//    val selectedImages by viewModel.selectedImages.collectAsState()
//    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
//    val error by viewModel.error.collectAsState()
//
//    val snackbarHostState = remember { SnackbarHostState() }
//
//    // 1. Modern Photo Picker (Append Logic)
//    val photoPickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.PickMultipleVisualMedia(3)
//    ) { newUris ->
//        if (newUris.isNotEmpty()) {
//            val combinedImages = (selectedImages + newUris).distinct().take(3)
//            viewModel.setSelectedImages(combinedImages)
//        }
//    }
//
//    LaunchedEffect(error) {
//        error?.let { snackbarHostState.showSnackbar(it) }
//    }
//
//    Scaffold(
//        snackbarHost = { SnackbarHost(snackbarHostState) },
//        topBar = {
//            CenterAlignedTopAppBar(
//                title = { Text("New Diagnosis") },
//                actions = {
//                    // 2. Preserved History Navigation
//                    IconButton(onClick = { navController.navigate("crop_health_history") }) {
//                        Icon(Icons.Default.History, contentDescription = "History")
//                    }
//                },
//                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.background
//                )
//            )
//        },
//        floatingActionButton = {
//            // 3. Floating Action Button for Analysis
//            if (selectedImages.isNotEmpty() && !isAnalyzing) {
//                ExtendedFloatingActionButton(
//                    onClick = {
//                        viewModel.analyze { result ->
//                            navController.currentBackStackEntry?.savedStateHandle?.set("analysis_result", result)
//                            navController.navigate("crop_health_detail")
//                        }
//                    },
//                    icon = { Icon(Icons.Outlined.CloudUpload, null) },
//                    text = { Text("Analyze Crop") },
//                    containerColor = MaterialTheme.colorScheme.primary
//                )
//            }
//        }
//    ) { padding ->
//        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
//
//            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//
//                // Instructions Card
//                Card(
//                    colors = CardDefaults.cardColors(
//                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
//                    ),
//                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Text(
//                            "Instructions",
//                            style = MaterialTheme.typography.labelLarge,
//                            fontWeight = FontWeight.Bold,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            "1. Capture close-up photos of the affected leaf.\n2. Upload 1-3 clear images.\n3. Ensure good lighting for best results.",
//                            style = MaterialTheme.typography.bodyMedium
//                        )
//                    }
//                }
//
//                // Image Grid Area
//                if (selectedImages.isEmpty()) {
//                    EmptyState(onAddClick = {
//                        photoPickerLauncher.launch(
//                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//                        )
//                    })
//                } else {
//                    Text(
//                        "Selected Images (${selectedImages.size}/3)",
//                        style = MaterialTheme.typography.titleMedium,
//                        modifier = Modifier.padding(bottom = 12.dp)
//                    )
//
//                    LazyVerticalGrid(
//                        columns = GridCells.Fixed(3),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        verticalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        items(selectedImages.size) { index ->
//                            ImageThumbnail(uri = selectedImages[index])
//                        }
//
//                        if (selectedImages.size < 3) {
//                            item {
//                                AddMoreButton {
//                                    photoPickerLauncher.launch(
//                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            // Loading Overlay
//            if (isAnalyzing) {
//                LoadingOverlay()
//            }
//        }
//    }
//}
//
//// --- Helper Composables ---
//
//@Composable
//fun EmptyState(onAddClick: () -> Unit) {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Icon(
//            imageVector = Icons.Default.AddPhotoAlternate,
//            contentDescription = null,
//            modifier = Modifier.size(80.dp),
//            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(
//            "No images selected",
//            style = MaterialTheme.typography.titleMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//        Button(onClick = onAddClick) {
//            Icon(Icons.Default.Add, contentDescription = null)
//            Spacer(modifier = Modifier.width(8.dp))
//            Text("Select Photos")
//        }
//    }
//}
//
//@Composable
//fun ImageThumbnail(uri: Uri) {
//    Box(modifier = Modifier.aspectRatio(1f)) {
//        AsyncImage(
//            model = uri,
//            contentDescription = null,
//            modifier = Modifier
//                .fillMaxSize()
//                .clip(RoundedCornerShape(12.dp)),
//            contentScale = ContentScale.Crop
//        )
//    }
//}
//
//@Composable
//fun AddMoreButton(onClick: () -> Unit) {
//    Box(
//        modifier = Modifier
//            .aspectRatio(1f)
//            .clip(RoundedCornerShape(12.dp))
//            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
//            .clickable { onClick() },
//        contentAlignment = Alignment.Center
//    ) {
//        Icon(
//            Icons.Default.Add,
//            contentDescription = "Add More",
//            tint = MaterialTheme.colorScheme.outline
//        )
//    }
//}
//
//@Composable
//fun LoadingOverlay() {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Black.copy(alpha = 0.6f))
//            .clickable(enabled = false) {},
//        contentAlignment = Alignment.Center
//    ) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            CircularProgressIndicator(color = MaterialTheme.colorScheme.primaryContainer)
//            Spacer(modifier = Modifier.height(16.dp))
//            Text(
//                "Analyzing Crop Health...",
//                style = MaterialTheme.typography.titleMedium,
//                color = Color.White
//            )
//            Text(
//                "This may take a few seconds",
//                style = MaterialTheme.typography.bodySmall,
//                color = Color.White.copy(alpha = 0.8f)
//            )
//        }
//    }
//}
package com.ai.egret.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ai.egret.viewmodels.CropHealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropHealthScreen(
    navController: NavController,
    farmId: Int,
    viewModel: CropHealthViewModel = hiltViewModel()
) {
    val selectedImages by viewModel.selectedImages.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val error by viewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(3)
    ) { newUris ->
        if (newUris.isNotEmpty()) {
            val combinedImages = (selectedImages + newUris).distinct().take(3)
            viewModel.setSelectedImages(combinedImages)
        }
    }

    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New Diagnosis") },
                actions = {
                    Button(
                        onClick = {
                            viewModel.analyze { result ->
                                // pass FULL RESULT (no ID needed)
                                navController.currentBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("analysis_result", result)

                                viewModel.setSelectedImages(emptyList())

                                // navigate WITHOUT id
                                navController.navigate("crop_health_detail")
                            }
                        },
                        enabled = !isAnalyzing
                    ) {
                        Text("Run Analysis")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = {   },
                    icon = { Icon(Icons.Outlined.Eco, contentDescription = null) },
                    label = { Text("Crop Health") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {  navController.navigate("soil_analysis/$farmId") },
                    icon = { Icon(Icons.Outlined.Layers, contentDescription = null) },
                    label = { Text("Soil Report") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

                // Instructions Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Instructions",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "1. Capture close-up photos of the affected leaf.\n2. Upload 1-3 clear images.\n3. Ensure good lighting for best results.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (selectedImages.isEmpty()) {
                    EmptyState(onAddClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    })
                } else {
                    Text(
                        "Selected Images (${selectedImages.size}/3)",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(selectedImages.size) { index ->
                            ImageThumbnail(uri = selectedImages[index])
                        }

                        if (selectedImages.size < 3) {
                            item {
                                AddMoreButton {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- THE FIX IS IN THIS BUTTON ---
                    Button(
                        onClick = {
                            viewModel.analyze { result ->
                                // 1. Pass result to details screen
                                navController.currentBackStackEntry?.savedStateHandle?.set("analysis_result", result)

                                // 2. CLEANUP: Clear inputs so they are gone when user returns
                                viewModel.setSelectedImages(emptyList())

                                // 3. Navigate
                                navController.navigate("crop_health_detail")
                            }
                        },
                        enabled = !isAnalyzing,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Analyzing...")
                        } else {
                            Icon(Icons.Outlined.Analytics, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Analysis")
                        }
                    }
                }
            }

            if (isAnalyzing) {
                LoadingOverlay()
            }
        }
    }
}

// --- Helper Composables ---

@Composable
fun EmptyState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AddPhotoAlternate,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No images selected",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select Photos")
        }
    }
}

@Composable
fun ImageThumbnail(uri: Uri) {
    Box(modifier = Modifier.aspectRatio(1f)) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun AddMoreButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "Add More",
            tint = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primaryContainer)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Analyzing Crop Health...",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                "This may take a few seconds",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}