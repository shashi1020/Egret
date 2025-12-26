package com.ai.egret.ui.screens


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ai.egret.R
import com.ai.egret.models.DailySummary
import com.ai.egret.ui.theme.M3Theme
import com.ai.egret.viewmodels.WeatherUiState
import com.ai.egret.viewmodels.WeatherViewModel


// Map weather string -> icon
private fun getWeatherIcon(weather: String): ImageVector {
    return when {
        weather.contains("Sunny", ignoreCase = true) || weather.contains("Clear", ignoreCase = true) -> Icons.Default.WbSunny
        weather.contains("Rain", ignoreCase = true) || weather.contains("Drizzle", ignoreCase = true) -> Icons.Default.WaterDrop
        weather.contains("Wind", ignoreCase = true) -> Icons.Default.Air
        else -> Icons.Default.Cloud
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    navController: NavController,
    // Hilt automatically injects the ViewModel
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState(initial = WeatherUiState.Loading)

    M3Theme {
        val scheme = MaterialTheme.colorScheme

        Scaffold(
            // Ensure content doesn't get cut off by status bars
            contentWindowInsets = WindowInsets.statusBars,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                stringResource(R.string.weather_title),
                                color = scheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                            Text(
                                stringResource(R.string.weather_subtitle),
                                color = scheme.onPrimaryContainer.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = scheme.primaryContainer,
                        titleContentColor = scheme.onPrimaryContainer
                    )
                )
            },
            containerColor = scheme.background
        ) { padding ->

            // Soft, theme-based gradient background
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                scheme.primary.copy(alpha = 0.08f),
                                scheme.background,
                                scheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                    )
            ) {
                when (val uiState = state) {
                    is WeatherUiState.Loading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = scheme.primary)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.weather_loading),
                                color = scheme.onBackground,
                                fontSize = 14.sp
                            )
                        }
                    }

                    is WeatherUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(90.dp)
                                    .graphicsLayer {
                                        shadowElevation = 16f
                                        shape = CircleShape
                                        clip = true
                                    },
                                color = scheme.errorContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Cloud,
                                        contentDescription = null,
                                        tint = scheme.onErrorContainer,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.weather_error_title),
                                color = scheme.onBackground,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.weather_error_msg),
                                color = scheme.onBackground.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "${stringResource(R.string.weather_error_prefix)} ${uiState.message}",
                                color = scheme.onBackground.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    is WeatherUiState.Success -> {
                        val daily = uiState.daily
                        val today = daily.firstOrNull()
                        val rest = if (daily.size > 1) daily.drop(1) else emptyList()

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            today?.let {
                                TodayHeroCard(it)
                                Spacer(Modifier.height(16.dp))
                            }

                            if (rest.isNotEmpty()) {
                                Text(
                                    text = stringResource(R.string.weather_next_days_header),
                                    color = scheme.onBackground,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                                )
                                // Use weight to allow the list to scroll within the remaining space
                                DailyList(rest, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodayHeroCard(today: DailySummary) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                shadowElevation = 18f
                shape = RoundedCornerShape(24.dp)
                clip = true
            },
        colors = CardDefaults.cardColors(containerColor = scheme.primaryContainer)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            scheme.primary.copy(alpha = 0.15f),
                            scheme.primaryContainer,
                            scheme.surface
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.weather_today_header),
                    color = scheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = today.predominantWeather.uppercase(),
                            color = scheme.onPrimaryContainer,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Max ${today.maxTemp}°C  |  Min ${today.minTemp}°C",
                            color = scheme.onPrimaryContainer.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.weather_avg_temp, today.avgTemp),
                            color = scheme.onPrimaryContainer.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Column(
                        modifier = Modifier.padding(start = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = getWeatherIcon(today.predominantWeather),
                            contentDescription = null,
                            tint = scheme.onPrimaryContainer,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = scheme.surface.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, scheme.secondary.copy(alpha = 0.8f))
                        ) {
                            Text(
                                text = stringResource(R.string.weather_rain_chance_format, (today.maxPop * 100).toInt()),
                                color = scheme.onSurface,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = scheme.onPrimaryContainer.copy(alpha = 0.2f))
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeatherDetailRowMarket(
                        icon = Icons.Default.WaterDrop,
                        label = stringResource(R.string.weather_total_rain),
                        value = "${today.totalRainMm}"
                    )
                    Spacer(Modifier.width(24.dp))
                    WeatherDetailRowMarket(
                        icon = Icons.Default.Air,
                        label = stringResource(R.string.weather_max_wind),
                        value = "${today.maxWind}"
                    )
                }
            }
        }
    }
}

@Composable
fun DailyList(daily: List<DailySummary>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(daily) { day ->
            DailySummaryItem(summary = day)
        }
    }
}

@Composable
fun DailySummaryItem(summary: DailySummary, isToday: Boolean = false) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                shadowElevation = if (isToday) 16f else 10f
                shape = RoundedCornerShape(18.dp)
                clip = true
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = summary.date.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = summary.predominantWeather,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurfaceVariant
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${summary.maxTemp}° / ${summary.minTemp}°C",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "AVG ${summary.avgTemp}°C",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = getWeatherIcon(summary.predominantWeather),
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.weather_item_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            HorizontalDivider(
                Modifier.padding(vertical = 12.dp),
                color = scheme.onSurfaceVariant.copy(alpha = 0.15f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    WeatherDetailRowMarket(
                        Icons.Default.WaterDrop,
                        stringResource(R.string.weather_rain_chance_label),
                        "${(summary.maxPop * 100).toInt()}%"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    WeatherDetailRowMarket(
                        Icons.Default.Air,
                        stringResource(R.string.weather_max_wind),
                        "${summary.maxWind}"
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    WeatherDetailRowMarket(
                        Icons.Default.DeviceThermostat,
                        stringResource(R.string.weather_avg_temp_label),
                        "${summary.avgTemp}"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    WeatherDetailRowMarket(
                        Icons.Default.WaterDrop,
                        stringResource(R.string.weather_total_rain),
                        "${summary.totalRainMm}"
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherDetailRowMarket(icon: ImageVector, label: String, value: String) {
    val scheme = MaterialTheme.colorScheme

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = scheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant.copy(alpha = 0.9f),
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface
        )
    }
}