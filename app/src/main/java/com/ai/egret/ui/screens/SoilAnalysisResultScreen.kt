package com.ai.egret.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ai.egret.models.SoilAnalysisResponseDto
import com.ai.egret.models.SoilRecommendationDto
import com.ai.egret.viewmodels.SoilReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoilAnalysisResultScreen(
    navController: NavController,
    viewModel: SoilReportViewModel = hiltViewModel()
) {
    val result by viewModel.result.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Soil Report") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    ErrorState(
                        message = error ?: "Unknown error",
                        onBack = { navController.popBackStack() }
                    )
                }
                result != null -> {
                    // Safe unwrapping done here
                    SoilAnalysisContent(result = result!!)
                }
                else -> {
                    // Fallback if result is null but not loading/error (shouldn't happen with correct VM logic)
                    ErrorState(
                        message = "No report data loaded",
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun SoilAnalysisContent(result: SoilAnalysisResponseDto) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- EXECUTIVE SUMMARY ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Executive Summary",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        result.advisory.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // --- SOIL PARAMETERS ---
        if (result.soil_analysis.isNotEmpty()) {
            item {
                Text(
                    "Nutrient Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(result.soil_analysis) { param ->
                ListItem(
                    headlineContent = { Text(param.name, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("Status: ${param.status ?: "N/A"}") },
                    trailingContent = {
                        Text(
                            "${param.value ?: "-"} ${param.unit ?: ""}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f)),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                )
            }
        } else {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Info, null)
                        Spacer(Modifier.width(12.dp))
                        Text("No specific nutrient values extracted from this report.")
                    }
                }
            }
        }

        // --- RECOMMENDATIONS ---
        item {
            Text(
                "AI Recommendations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(result.advisory.recommendations) { rec ->
            RecommendationCard(rec)
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
fun ErrorState(message: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text(message, color = Color.Gray)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBack) { Text("Go Back") }
        }
    }
}

// Reuse your existing RecommendationCard Composable here
@Composable
fun RecommendationCard(rec: SoilRecommendationDto) {
    val priorityColor = when (rec.priority.uppercase()) {
        "HIGH" -> MaterialTheme.colorScheme.error
        "MEDIUM" -> Color(0xFFFFA000) // Amber
        else -> Color(0xFF388E3C)     // Green
    }

    val containerColor = priorityColor.copy(alpha = 0.05f)
    val borderColor = priorityColor.copy(alpha = 0.3f)

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Issue Title
                Row(modifier = Modifier.weight(1f)) {
                    Icon(
                        if(rec.priority == "HIGH") Icons.Default.Warning else Icons.Outlined.Spa,
                        contentDescription = null,
                        tint = priorityColor,
                        modifier = Modifier.size(20.dp).padding(top=2.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        rec.issue,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Priority Badge
                SuggestionChip(
                    onClick = {},
                    label = { Text(rec.priority) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        labelColor = priorityColor
                    ),
                    // --- FIX HERE: Use BorderStroke directly ---
                    border = androidx.compose.foundation.BorderStroke(1.dp, priorityColor),
                    modifier = Modifier.height(28.dp)
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = borderColor
            )

            Text(
                rec.action,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}