package com.ai.egret.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Coronavirus
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ai.egret.models.UserDiseaseHistoryDto
import com.ai.egret.viewmodels.CropHealthHistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropHealthHistoryScreen(
    navController: NavController,
    vm: CropHealthHistoryViewModel = hiltViewModel()
) {
    val history by vm.history.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Disease History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (history.isEmpty()) {
                EmptyDiseaseState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Past Detections",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(
                        items = history,
                        key = { it.analysis_id } // ✅ STABLE KEY
                    ) { item ->
                        DiseaseHistoryCard(
                            item = item,
                            onClick = {
                                // ✅ HISTORY → DB DETAIL
                                navController.navigate(
                                    "crop_health_detail/${item.analysis_id}"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiseaseHistoryCard(
    item: UserDiseaseHistoryDto,
    onClick: () -> Unit
) {
    val isFrequent = item.detection_count > 3
    val iconColor =
        if (isFrequent) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary
    val iconBg = iconColor.copy(alpha = 0.1f)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // ✅ CLICK ENABLED
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector =
                        if (isFrequent) Icons.Outlined.Coronavirus
                        else Icons.Filled.BugReport,
                    contentDescription = null,
                    tint = iconColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.disease_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                item.scientific_name?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("${item.detection_count} detections") },
                        icon = { Icon(Icons.Outlined.History, null) }
                    )

                    SuggestionChip(
                        onClick = {},
                        label = { Text(item.last_detected_at.take(10)) },
                        icon = { Icon(Icons.Outlined.CalendarToday, null) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyDiseaseState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.Verified,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Clean Bill of Health", fontWeight = FontWeight.Bold)
        Text("No disease history recorded yet.")
    }
}
