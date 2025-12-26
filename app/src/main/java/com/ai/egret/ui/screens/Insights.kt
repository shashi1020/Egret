package com.ai.egret.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ai.egret.models.*
import com.ai.egret.utils.formatDatePretty
import com.ai.egret.utils.toPercentage
import com.ai.egret.viewmodels.FarmInsightsUiState
import com.ai.egret.viewmodels.FarmInsightsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmInsightsScreen(
    navController: NavController,
    farmId: Int,
    viewModel: FarmInsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(farmId) {
        viewModel.loadInsights(farmId = farmId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crop Doctor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadInsights(farmId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            when (val state = uiState) {
                is FarmInsightsUiState.Loading -> LoadingView()
                is FarmInsightsUiState.Error -> ErrorView(state.message) { viewModel.loadInsights(farmId) }
                is FarmInsightsUiState.Loaded -> {
                    // Pass the natural language summary string here
                    InsightsContent(
                        response = state.data,
                        historySummary = state.historySummary
                    )
                }
            }
        }
    }
}

@Composable
fun InsightsContent(
    response: FarmInsightsResponse,
    historySummary: String
) {
    val advisory = response.advisory

    // Determine Theme Colors based on Severity
    val severityColor = getSeverityColor(advisory.severity)
    val containerColor = severityColor.copy(alpha = 0.1f)

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HERO SECTION: "Is my crop OK?"
        item {
            HeroStatusCard(
                advisory = advisory,
                farmName = response.farm.name ?: "My Farm",
                mainColor = severityColor,
                containerColor = containerColor
            )
        }

        // 2. DIAGNOSIS SECTION: "Why is this happening?"
        item {
            DiagnosisCard(advisory = advisory)
        }

        // 3. ACTION SECTION: "What should I do?"
        item {
            ActionPlanCard(actions = advisory.recommended_actions, mainColor = severityColor)
        }

        // 4. HISTORY SECTION: "Trend" (Natural Language)
        item {
            HistorySummaryCard(summary = historySummary, history = response.history)
        }

        // Bottom Spacer
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

// -----------------------------------------------------------------------------
// 1. HERO CARD (Visual Impact)
// -----------------------------------------------------------------------------
@Composable
fun HeroStatusCard(
    advisory: AdvisoryDto,
    farmName: String,
    mainColor: Color,
    containerColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = farmName.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = getStatusTitle(advisory.severity),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = mainColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            // The "Pie Chart" / Gauge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Stress Score Gauge
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedCircularGauge(
                        value = advisory.stress_score.toFloat(), // 0.0 to 1.0
                        color = mainColor,
                        size = 120.dp,
                        strokeWidth = 12.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Stress Level", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }

                // Confidence Stats
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatRow(
                        label = "Confidence",
                        value = advisory.confidence_score.toPercentage(),
                        icon = Icons.Default.Info
                    )
                    StatRow(
                        label = "Coverage",
                        value = advisory.explanation.confidence_rationale.coverage.toPercentage(),
                        icon = Icons.Default.CheckCircle
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 2. DIAGNOSIS CARD (Why?)
// -----------------------------------------------------------------------------
@Composable
fun DiagnosisCard(advisory: AdvisoryDto) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(title = "Analysis", icon = Icons.Default.Info)

            // Main Cause
            if (advisory.explanation.causes.isNotEmpty()) {
                Text(
                    text = advisory.explanation.causes.first(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Specific Signals (Pie/Bar representations)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // NDVI Variance (Patchiness)
                val variance = advisory.signals.ndvi_variance ?: 0.0
                SignalMiniCard(
                    modifier = Modifier.weight(1f),
                    title = "Patchiness",
                    value = variance,
                    isGood = variance < 0.3 // Arbitrary threshold for demo
                )

                // Prop Low (Affected Area)
                val propLow = advisory.signals.prop_low ?: 0.0
                SignalMiniCard(
                    modifier = Modifier.weight(1f),
                    title = "Area Affected",
                    value = propLow,
                    isGood = propLow < 0.2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Triggers (Bullet points)
            advisory.explanation.triggers.forEach { trigger ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("•", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                    Text(text = trigger, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun SignalMiniCard(modifier: Modifier = Modifier, title: String, value: Double, isGood: Boolean) {
    val color = if (isGood) Color(0xFF4CAF50) else Color(0xFFFF9800)
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value.toPercentage(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        // Simple progress bar
        LinearProgressIndicator(
            progress = { value.toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 4.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

// -----------------------------------------------------------------------------
// 3. ACTION PLAN CARD (What to do?)
// -----------------------------------------------------------------------------
@Composable
fun ActionPlanCard(actions: List<String>, mainColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(title = "Recommended Actions", icon = Icons.Default.CheckCircle)

            if (actions.isEmpty()) {
                Text("No specific actions required at this time.", style = MaterialTheme.typography.bodyMedium)
            } else {
                actions.forEach { action ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Checkbox visual
                        Icon(
                            imageVector = Icons.Default.CheckCircle, // Or empty square
                            contentDescription = null,
                            tint = mainColor,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = action,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 4. HISTORY CARD (Natural Language)
// -----------------------------------------------------------------------------
@Composable
fun HistorySummaryCard(summary: String, history: List<HistoryItemDto>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(title = "Timeline", icon = Icons.Default.DateRange)

            // The "Smart" Sentence
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Minimal list for context (Last 3 entries)
            history.take(3).forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.timestamp?.formatDatePretty() ?: "Unknown Date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Score: ${(item.health_score ?: 0.0).toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// HELPER COMPONENTS
// -----------------------------------------------------------------------------

@Composable
fun AnimatedCircularGauge(
    value: Float, // 0.0 to 1.0
    color: Color,
    size: Dp,
    strokeWidth: Dp
) {
    // Animate the sweep angle
    val animatedProgress by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 1000)
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background Track
            drawArc(
                color = Color.LightGray.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            // Foreground Progress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        // Text inside
        Text(
            text = value.toDouble().toPercentage(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatRow(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(msg: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(msg, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Try Again") }
    }
}

// -----------------------------------------------------------------------------
// UTILS
// -----------------------------------------------------------------------------

fun getSeverityColor(severity: String): Color {
    return when (severity.uppercase()) {
        "CRITICAL" -> Color(0xFFD32F2F) // Red
        "WARNING" -> Color(0xFFFFA000)  // Amber
        else -> Color(0xFF388E3C)       // Green
    }
}

fun getStatusTitle(severity: String): String {
    return when (severity.uppercase()) {
        "CRITICAL" -> "Action Required"
        "WARNING" -> "Check Crop"
        else -> "Crop Healthy"
    }
}