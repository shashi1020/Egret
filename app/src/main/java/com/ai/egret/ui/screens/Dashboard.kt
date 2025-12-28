package com.ai.egret.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Coronavirus
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ai.egret.R
import com.ai.egret.models.FarmDto
import com.ai.egret.viewmodels.MyFarmsViewModel
import com.ai.egret.ui.theme.M3Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDashboard(
    navController: NavController,
    viewModel: MyFarmsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFarms()
    }

    val hasRegisteredFields = uiState.farms.isNotEmpty()

    M3Theme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { EgretDashboardTopBar(navController) }
        ) { padding ->

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    // 1. Hero / Greeting
                    item { DashboardHeaderSection() }

                    // 2. Action Grid (Updated with Disease History)
                    item { ActionGridSection(navController) }

                    // 3. Recent Farms Header
                    item {
                        SectionHeader(
                            title = stringResource(R.string.dashboard_recent_farms),
                            actionText = stringResource(R.string.dashboard_view_all),
                            onActionClick = { navController.navigate("my_farms") }
                        )
                    }

                    // 4. Farm List
                    if (!hasRegisteredFields) {
                        item { EmptyFarmStateCard(navController) }
                    } else {
                        items(uiState.farms.take(3)) { farm ->
                            FarmDashboardCard(
                                farm = farm,
                                onClick = { navController.navigate("farm_insights/${farm.id}") }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SECTIONS
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EgretDashboardTopBar(navController: NavController) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "EGRET",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        actions = {
            IconButton(onClick = { navController.navigate("notifications") }) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
            }
            IconButton(onClick = { navController.navigate("settings") }) {
                Icon(Icons.Outlined.Settings, contentDescription = "Settings")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun DashboardHeaderSection() {
    Column {
        Text(
            text = stringResource(R.string.dashboard_greeting),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.dashboard_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ActionGridSection(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Primary Action: Add Farm
        DashboardActionCard(
            title = stringResource(R.string.dashboard_add_farm),
            subtitle = stringResource(R.string.dashboard_add_farm_desc),
            icon = Icons.Default.AddLocationAlt,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onClick = { navController.navigate("register_field") }
        )

        // Row 1: My Farms & Soil Reports
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                DashboardActionCard(
                    title = stringResource(R.string.dashboard_my_farms),
                    subtitle = "Manage crops",
                    icon = Icons.Outlined.Agriculture,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = { navController.navigate("my_farms") }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                DashboardActionCard(
                    title = "Soil Reports",
                    subtitle = "Lab history",
                    icon = Icons.Outlined.Analytics,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = { navController.navigate("soil_analysis_history") }
                )
            }
        }

        // Row 2: Disease History & Market Prices (New!)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                DashboardActionCard(
                    title = "Disease History",
                    subtitle = "Past detections",
                    icon = Icons.Outlined.Coronavirus, // Looks like a virus/bug
                    backgroundColor = Color(0xFFFFEBEE), // Light Red
                    contentColor = Color(0xFFD32F2F),     // Dark Red
                    onClick = { navController.navigate("crop_health_history") }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                DashboardActionCard(
                    title = "Market Prices",
                    subtitle = "Live rates",
                    icon = Icons.Outlined.Storefront,
                    backgroundColor = Color(0xFFE8F5E9), // Light Green
                    contentColor = Color(0xFF2E7D32),    // Dark Green
                    onClick = { navController.navigate("market_prices") }
                )
            }
        }
    }
}

@Composable
fun DashboardActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.height(110.dp).fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Icon Background Circle
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(40.dp)
                    .background(contentColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = contentColor)
            }

            // Text Content
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onActionClick) {
            Text(text = actionText, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun FarmDashboardCard(farm: FarmDto, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Grass,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = farm.name ?: "Unnamed Farm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ID: ${farm.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
fun EmptyFarmStateCard(navController: NavController) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("register_field") },
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "No farms registered yet",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Tap here to add your first field",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}