package com.ai.egret.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ai.egret.viewmodels.SoilAnalysisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoilAnalysisScreen(
    navController: NavController,
    farmId: Int,
    viewModel: SoilAnalysisViewModel = hiltViewModel()
) {
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val result by viewModel.result.collectAsState()

    // Local state to hold the selected file before analysis
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPdfName by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // PDF Picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPdfUri = uri
            selectedPdfName = getFileName(context, uri)
        }
    }

    // Handle Success Navigation
    LaunchedEffect(result) {
        result?.meta?.report_id?.let { reportId ->
            navController.navigate("soil_analysis_result/$reportId") {
                popUpTo("soil_analysis/$farmId") { inclusive = false }
            }
            viewModel.onResultConsumed()
        }
    }


    // Handle Errors
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(

        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Soil Doctor") },
                actions = {
                    IconButton(onClick = { /* Navigate to soil history if needed */ }) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                // 1. CROP HEALTH TAB (Inactive -> Navigate)
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        // FIX: Navigate back to Crop Health and clear stack to avoid A->B->A->B loops
                        navController.navigate("crop_health/$farmId") {
                            // Pop everything up to the destination to keep stack clean
                            popUpTo("crop_health/$farmId") { inclusive = true }
                        }
                    },
                    icon = { Icon(Icons.Outlined.Eco, contentDescription = null) },
                    label = { Text("Crop Health") }
                )

                // 2. SOIL REPORT TAB (Active)
                NavigationBarItem(
                    selected = true,
                    onClick = {
                        // FIX: Do nothing! We are already here.
                    },
                    icon = { Icon(Icons.Outlined.Layers, contentDescription = null) },
                    label = { Text("Soil Report") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

                // 1. INSTRUCTIONS
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Upload Lab Report",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Upload your soil testing lab report (PDF) to get AI-driven fertilizer recommendations and crop suitability analysis.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // 2. UPLOAD AREA (Empty vs Selected)
                if (selectedPdfUri == null) {
                    UploadPlaceholder(
                        onClick = { launcher.launch("application/pdf") }
                    )
                } else {
                    SelectedFileCard(
                        fileName = selectedPdfName ?: "Unknown File",
                        onRemove = {
                            selectedPdfUri = null
                            selectedPdfName = null
                        },
                        onChange = { launcher.launch("application/pdf") }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 3. ANALYZE BUTTON
                Button(
                    onClick = {
                        selectedPdfUri?.let { viewModel.analyze(it) }
                    },
                    enabled = selectedPdfUri != null && !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Processing Report...")
                    } else {
                        Icon(Icons.Outlined.Analytics, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze Soil Report")
                    }
                }
            }
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
fun UploadPlaceholder(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            ) // You can use a dashed border modifier here if you have one
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.CloudUpload,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Tap to select PDF",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Max size: 5MB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SelectedFileCard(fileName: String, onRemove: () -> Unit, onChange: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PDF Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp)), // Light Red bg
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "PDF",
                    tint = Color(0xFFD32F2F) // Red tint
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // File Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ready to analyze",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50) // Green text
                )
            }

            // Remove Action
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Change File Link
        Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChange() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Change File",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// --- HELPER: Get Filename from URI ---
fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "unknown_file.pdf"
}