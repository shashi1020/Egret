package com.ai.egret.ui.screens


import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ai.egret.R
import com.ai.egret.models.MarketPriceResponse
import com.ai.egret.utils.RequestAndFetchLocation // Ensure this file exists in your utils package
import com.ai.egret.viewmodels.MarketPriceViewModel


// Ideally, put this in BuildConfig or local.properties
private const val GOV_API_KEY = "579b464db66ec23bdd000001dd9754baa209481c724a3f750b2ea523"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketPriceScreen(
    navController: NavController,
    // Hilt automatically injects the ViewModel with the Repository
    viewModel: MarketPriceViewModel = hiltViewModel()
) {
    // --- State vars ---
    var commodityQuery by remember { mutableStateOf("") }
    var marketQuery by remember { mutableStateOf("") }
    var stateQuery by remember { mutableStateOf("") }
    var districtQuery by remember { mutableStateOf("") }

    var locationFetched by remember { mutableStateOf(false) }
    var userState by remember { mutableStateOf("") }
    var userDistrict by remember { mutableStateOf("") }

    var showFilterDialog by remember { mutableStateOf(false) }

    // Observe ViewModel State
    val prices by viewModel.prices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Scroll Behavior
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // --- Location fetching ---
    if (!locationFetched) {
        RequestAndFetchLocation { _, _, state, district ->
            state?.let {
                stateQuery = it
                userState = it
            }
            district?.let {
                districtQuery = it
                userDistrict = it
            }
            locationFetched = true
            if (state == null && district == null) {
                userState = "Location not found" // Fallback string, logic handled in UI
            }
        }
    }

    // --- Data fetching ---
    LaunchedEffect(Unit) {
        // Trigger load only if empty (or force refresh logic if needed)
        if (prices.isEmpty()) {
            viewModel.loadPrices(GOV_API_KEY)
        }
    }

    // Local filtering (Client side)
    val filteredPrices = remember(prices, commodityQuery, marketQuery, stateQuery) {
        prices.filter { price ->
            price.commodity.contains(commodityQuery, ignoreCase = true) &&
                    (marketQuery.isBlank() || price.market.contains(marketQuery, ignoreCase = true)) &&
                    (stateQuery.isBlank() || price.state.contains(stateQuery, ignoreCase = true))
        }
    }

    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = scheme.background,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.market_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = scheme.onPrimaryContainer
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = scheme.primaryContainer,
                    titleContentColor = scheme.onPrimaryContainer,
                    actionIconContentColor = scheme.onPrimaryContainer
                ),
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(scheme.background)
        ) {
            // Search bar
            OutlinedTextField(
                value = commodityQuery,
                onValueChange = { commodityQuery = it },
                placeholder = { Text(stringResource(R.string.market_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (commodityQuery.isNotEmpty()) {
                        IconButton(onClick = { commodityQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(28.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = scheme.surface,
                    unfocusedContainerColor = scheme.surface
                ),
                shape = RoundedCornerShape(28.dp)
            )

            // Filter button
            OutlinedButton(
                onClick = { showFilterDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.primary),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = SolidColor(scheme.primary)
                ),
                contentPadding = PaddingValues(12.dp)
            ) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.market_filter_btn), style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content Section
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = scheme.primary,
                            trackColor = scheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.market_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }

                error != null -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = scheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "${stringResource(R.string.market_error_prefix)} $error",
                            color = scheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                else -> {
                    Text(
                        text = stringResource(R.string.market_results_found, filteredPrices.size),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onBackground,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                    )

                    if (filteredPrices.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = scheme.outline,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.market_no_results),
                                color = scheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        // CRUCIAL: Modifier.weight(1f) allows scrolling
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f, fill = true)
                        ) {
                            items(filteredPrices) { price ->
                                MarketPriceItemCard(price = price)
                            }
                        }
                    }
                }
            }

            // Filter Dialog
            if (showFilterDialog) {
                AlertDialog(
                    onDismissRequest = { showFilterDialog = false },
                    title = { Text(stringResource(R.string.market_filter_btn)) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = marketQuery,
                                onValueChange = { marketQuery = it },
                                label = { Text(stringResource(R.string.market_optional_market)) },
                                leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = stateQuery,
                                onValueChange = { stateQuery = it },
                                label = { Text(stringResource(R.string.market_optional_state)) },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = districtQuery,
                                onValueChange = { districtQuery = it },
                                label = { Text(stringResource(R.string.market_optional_district)) },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showFilterDialog = false }) {
                            Text(stringResource(R.string.market_apply))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showFilterDialog = false }) {
                            Text(stringResource(R.string.market_cancel))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MarketPriceItemCard(price: MarketPriceResponse) {
    val scheme = MaterialTheme.colorScheme

    Card(
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .wrapContentHeight()
        ) {
            // Market & Location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = scheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = price.market,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurface
                    )
                }
                Text(
                    text = "${price.district.trim()}, ${price.state.trim()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = scheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Commodity Details
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Crop,
                    contentDescription = null,
                    tint = scheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${price.commodity.trim()} (${price.variety.trim()})",
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.market_modal_price),
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${price.modalPrice} / 100kg",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary
                    )
                }

                val pricePerKg = price.modalPrice.toDoubleOrNull()?.div(100.0) ?: 0.0
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.market_approx_kg),
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹%.2f".format(pricePerKg),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Arrival Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${stringResource(R.string.market_arrival_date)} ${price.arrivalDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}