package com.example.projectandroidstarwars.ui.theme

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectandroidstarwars.domain.planets.Planet
import com.example.projectandroidstarwars.viewmodel.LoadState
import com.example.projectandroidstarwars.viewmodel.PlanetsViewModel
import com.example.projectandroidstarwars.viewmodel.planetsViewModelFactory

@Composable
fun PlanetsScreen() {
    val planetsViewModel: PlanetsViewModel = viewModel(
        factory = planetsViewModelFactory
    )

    val showDetails = planetsViewModel.selectedPlanetId != null

    BackHandler(enabled = showDetails) {
        planetsViewModel.backToList()
    }

    if (showDetails) {
        Column(Modifier.fillMaxSize()) {
            TextButton(
                onClick = planetsViewModel::backToList
            ) {
                Text("← К списку планет")
            }

            when (val state = planetsViewModel.detailsState) {
                LoadState.Loading -> PlanetLoading()

                is LoadState.Error -> PlanetError(
                    message = state.message,
                    onRetry = planetsViewModel::retryDetails
                )

                is LoadState.Success -> PlanetDetails(state.data)
            }
        }
    } else {
        PlanetList(
            state = planetsViewModel.listState,
            searchQuery = planetsViewModel.searchQuery,
            onSearchChange = planetsViewModel::updateSearchQuery,
            onSearch = planetsViewModel::search,
            onClear = planetsViewModel::clearSearch,
            onRefresh = planetsViewModel::refresh,
            onPlanetClick = planetsViewModel::openPlanet
        )
    }
}

@Composable
private fun PlanetList(
    state: LoadState<List<Planet>>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onRefresh: () -> Unit,
    onPlanetClick: (Int) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 16.dp),
            label = {
                Text("Планета на английском, например Naboo")
            },
            singleLine = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSearch()
                }
            ) {
                Text("Найти")
            }

            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    onClear()
                }
            ) {
                Text("Очистить")
            }

            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    onRefresh()
                },
                enabled = state !is LoadState.Loading
            ) {
                Text("Обновить")
            }
        }

        when (state) {
            LoadState.Loading -> PlanetLoading()

            is LoadState.Error -> PlanetError(
                message = state.message,
                onRetry = onRefresh
            )

            is LoadState.Success -> {
                val planets = state.data

                Text(
                    text = "Загружено: ${planets.size}",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.labelLarge
                )

                if (planets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Планеты не найдены.\n" +
                                    "Измените запрос или нажмите «Очистить».",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = planets,
                            key = { it.id }
                        ) { planet ->
                            Card(
                                onClick = {
                                    onPlanetClick(planet.id)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    PlanetImage(planet)

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = planet.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = "Климат: ${planet.climate}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        Text(
                                            text = "Население: ${planet.population}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Text("›")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanetImage(
    planet: Planet,
    size: Dp = 72.dp
) {
    val imageResource = planetImageResource(planet.id)

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (imageResource != null) {
            Image(
                painter = painterResource(imageResource),
                contentDescription = planet.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = "◎",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PlanetDetails(planet: Planet) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            PlanetImage(planet, size = 220.dp)
        }

        Text(
            text = planet.name,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        PlanetInfo("Климат", planet.climate)
        PlanetInfo("Поверхность", planet.terrain)
        PlanetInfo("Население", planet.population)
        PlanetInfo("Диаметр", planetMeasurement(planet.diameter, "км"))
        PlanetInfo("Гравитация", planet.gravity)
        PlanetInfo(
            "Период вращения",
            planetMeasurement(planet.rotationPeriod, "ч")
        )
        PlanetInfo(
            "Орбитальный период",
            planetMeasurement(planet.orbitalPeriod, "дней")
        )
        PlanetInfo(
            "Поверхность, покрытая водой",
            planetMeasurement(planet.surfaceWater, "%")
        )
        PlanetInfo("Фильмов в SWAPI", planet.filmCount.toString())

        Text(
            text = "Данные: swapi.dev. Неизвестные значения отмечены отдельно.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun planetMeasurement(value: String, unit: String): String {
    return if (value.replace(",", "").toDoubleOrNull() != null) {
        "$value $unit"
    } else {
        value
    }
}

@Composable
private fun PlanetInfo(title: String, value: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun PlanetLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()

        Text(
            text = "Загружаем планеты…",
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun PlanetError(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(onClick = onRetry) {
            Text("Повторить")
        }
    }
}