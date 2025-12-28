package com.ai.egret.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ai.egret.models.AnalysisResponseDto
import com.ai.egret.models.RecommendedActionsDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropHealthDetailScreen(navController: NavController) {
    // 1. Safe Argument Retrieval
    val analysis = remember {
        navController.previousBackStackEntry?.savedStateHandle?.get<AnalysisResponseDto>("analysis_result")
    }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 2. Handle Null State Gracefully
    if (analysis == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No analysis data found.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnosis Report") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            DiagnosisHeader(analysis)

            ConfidenceSection(analysis)

            analysis.advisory?.recommended_actions?.let { actions ->
                ActionPlanSection(actions)
            }

            if (analysis.reference_images.isNotEmpty()) {
                Text(
                    "Visual Match References",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(analysis.reference_images.size) { idx ->
                        val ref = analysis.reference_images[idx]
                        ReferenceImageCard(
                            imageUrl = ref.thumbnail_url ?: ref.image_url,
                            similarity = ref.similarity,
                            citation = ref.citation,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ref.image_url))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DiagnosisHeader(analysis: AnalysisResponseDto) {
    val disease = analysis.detection_summary?.disease_detected ?: "Unknown"
    val scientific = analysis.detection_summary?.scientific_name
    val isHealthy = disease.equals("Healthy", ignoreCase = true)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isHealthy)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )

    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (isHealthy) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (isHealthy) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = disease,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isHealthy) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                )
                if (scientific != null) {
                    Text(
                        text = scientific,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = if (isHealthy) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = {},
                    label = {
                        Text(analysis.advisory?.severity_assessment?.take(20) + "..." ?: "Severity Unknown")
                    },
                    leadingIcon = { Icon(Icons.Outlined.Info, null, modifier = Modifier.size(16.dp)) }
                )
            }
        }
    }
}

@Composable
fun ConfidenceSection(analysis: AnalysisResponseDto) {
    val confidence = analysis.detection_summary?.disease_probability ?: 0.0
    val isLowConfidence = confidence < 0.6 // Threshold
    val confidencePercent = (confidence * 100).toInt()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("AI Confidence", style = MaterialTheme.typography.labelLarge)
            Text("$confidencePercent%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }

        LinearProgressIndicator(
            progress = { confidence.toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = if (isLowConfidence) Color(0xFFFF9800) else Color(0xFF4CAF50),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        AnimatedVisibility(visible = isLowConfidence) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF57C00))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = analysis.confidence_note?.message ?: "Low confidence. Try uploading clearer images.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE65100)
                    )
                }
            }
        }
    }
}

@Composable
fun ActionPlanSection(actions: RecommendedActionsDto) {
    Text("Treatment Plan", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

    if (actions.immediate_treatment.isNotEmpty()) {
        ActionGroupCard(
            title = "Immediate Action",
            icon = Icons.Outlined.MedicalServices,
            color = MaterialTheme.colorScheme.errorContainer,
            items = actions.immediate_treatment
        )
    }

    if (actions.chemical_or_biological_control.isNotEmpty()) {
        ActionGroupCard(
            title = "Chemical & Biological",
            icon = Icons.Outlined.Science,
            color = MaterialTheme.colorScheme.secondaryContainer,
            items = actions.chemical_or_biological_control
        )
    }

    if (actions.preventive_care_next_stage.isNotEmpty()) {
        ActionGroupCard(
            title = "Prevention (Next Stage)",
            icon = Icons.Outlined.Spa,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            items = actions.preventive_care_next_stage
        )
    }
}

@Composable
fun ActionGroupCard(title: String, icon: ImageVector, color: Color, items: List<String>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                    Text("•", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 8.dp))
                    Text(item, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun ReferenceImageCard(imageUrl: String, similarity: Double?, citation: String?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Reference Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Match: ${(similarity?.times(100))?.toInt() ?: 0}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = citation ?: "Unknown Source",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}