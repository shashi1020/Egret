package com.ai.egret.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    viewModel: MyFarmsViewModel = hiltViewModel() // Inject ViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    // Refresh data when screen appears
    LaunchedEffect(Unit) {
        viewModel.loadFarms()
    }

    val hasRegisteredFields = uiState.farms.isNotEmpty()

    M3Theme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { EgretDashboardTopBar(navController) },
            floatingActionButton = {
                if (hasRegisteredFields) {
                    AddFieldFAB(navController)
                }
            }
        ) { padding ->

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .fillMaxSize()
                ) {

                    Text(
                        text = stringResource(R.string.dashboard_greeting),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.dashboard_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    QuickActionsRow(
                        navController = navController,
                        hasRegisteredFields = hasRegisteredFields
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (hasRegisteredFields) {
                        FieldListSection(navController, uiState.farms)
                    } else {
                        FieldRegistrationBanner(navController)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        stringResource(R.string.dashboard_explore),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EgretDashboardTopBar(navController: NavController) {
    TopAppBar(
        title = {
            Text(
                "EGRET",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        // [Optional] Removed navigationIcon if no drawer exists
        actions = {
            IconButton(onClick = { navController.navigate("notifications") }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            IconButton(onClick = { navController.navigate("settings") }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldRegistrationBanner(navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        onClick = { navController.navigate("register_field") }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.dashboard_get_started),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    stringResource(R.string.dashboard_register_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Button(
                    onClick = { navController.navigate("register_field") },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.register_btn))
                }
            }
            Icon(
                imageVector = Icons.Default.AddLocationAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(80.dp)
                    .padding(start = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Added for onClick on Card
@Composable
fun QuickActionsRow(
    navController: NavController,
    hasRegisteredFields: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Register new field
        ElevatedCard(
            modifier = Modifier
                .weight(1f)
                .height(100.dp),
            shape = RoundedCornerShape(18.dp),
            onClick = { navController.navigate("register_field") }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AddLocationAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.dashboard_add_farm),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.dashboard_add_farm_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 12.sp
                    )
                }
            }
        }

        // My Farms
        ElevatedCard(
            modifier = Modifier
                .weight(1f)
                .height(100.dp),
            shape = RoundedCornerShape(18.dp),
            onClick = {
                navController.navigate("my_farms")
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Crop,
                    contentDescription = null,
                    tint = if (hasRegisteredFields) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.dashboard_my_farms),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (hasRegisteredFields) stringResource(R.string.dashboard_view_registered) else stringResource(R.string.dashboard_no_farms),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FieldListSection(navController: NavController, fields: List<FarmDto>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.dashboard_recent_farms),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextButton(
                onClick = { navController.navigate("my_farms") }
            ) {
                Text(
                    text = stringResource(R.string.dashboard_view_all),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp) // Fixed height for dashboard preview
        ) {
            // Only show top 3 farms on dashboard
            items(fields.take(3)) { field ->
                FieldItem(field = field, onClick = {
                    // Navigate to Farm Insights using correct ID type
                    navController.navigate("farm_insights/${field.id}")
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldItem(field: FarmDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = field.name ?: "Unknown Farm",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Note: FarmDto might not have NDVI status yet.
                // You can add logic here if your API returns it, otherwise show location or area.
                Text(
                    text = "ID: ${field.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Use standard icon instead of missing drawable
            Icon(
                imageVector = Icons.Default.Crop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun AddFieldFAB(navController: NavController) {
    FloatingActionButton(
        onClick = { navController.navigate("register_field") },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add Farm")
    }
}