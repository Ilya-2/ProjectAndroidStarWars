@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.projectandroidstarwars.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.projectandroidstarwars.domain.model.StarWarsCharacter
import com.example.projectandroidstarwars.ui.characterImageResource
import com.example.projectandroidstarwars.viewmodel.*
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel

private data class BottomTab(
    val route: String,
    val title: String,
    val symbol: String
)

private val bottomTabs = listOf(
    BottomTab("people", "Персонажи", "★"),
    BottomTab("planets", "Планеты", "◎"),
    BottomTab("favorites", "Избранное", "♥"),
    BottomTab("settings", "Настройки", "⚙")
)

@Composable
fun StarWarsApp(viewModel: CharactersViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val route = destination?.route

    val favoritesViewModel: FavoritesViewModel = composeViewModel(
        factory = favoritesViewModelFactory
    )

    val filtersViewModel: FiltersViewModel = composeViewModel(
        factory = filtersViewModelFactory
    )

    val badgeVisible by viewModel.badgeVisible.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val favoriteError = favoritesViewModel.actionError

    LaunchedEffect(favoriteError) {
        if (favoriteError != null) {
            snackbarHostState.showSnackbar(favoriteError)
            favoritesViewModel.dismissError()
        }
    }

    fun openTab(target: String) {
        navController.navigate(target) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val title = when (route) {
        "details/{characterId}" -> "Досье персонажа"
        "planets" -> "Планеты"
        "favorites" -> "Избранное"
        "settings" -> "Настройки поиска"
        else -> "Архив галактики"
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (route == "details/{characterId}") {
                        TextButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Назад")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                bottomTabs.forEach { tab ->
                    val selected = destination
                        ?.hierarchy
                        ?.any { it.route == tab.route } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                openTab(tab.route)
                            }
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (
                                        tab.route == "settings" &&
                                        badgeVisible
                                    ) {
                                        Badge()
                                    }
                                }
                            ) {
                                Text(tab.symbol)
                            }
                        },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "people",
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        ) {
            navigation(
                startDestination = "characters",
                route = "people"
            ) {
                composable("characters") {
                    CharacterListScreen(
                        viewModel = viewModel,
                        favoritesViewModel = favoritesViewModel,
                        onOpenFilters = {
                            openTab("settings")
                        },
                        onCharacterClick = { id ->
                            navController.navigate("details/$id")
                        }
                    )
                }

                composable(
                    route = "details/{characterId}",
                    arguments = listOf(
                        navArgument("characterId") {
                            type = NavType.IntType
                        }
                    )
                ) { entry ->
                    val detailsViewModel: CharacterDetailsViewModel =
                        composeViewModel(
                            viewModelStoreOwner = entry,
                            factory = characterDetailsViewModelFactory
                        )

                    when (val state = detailsViewModel.state) {
                        LoadState.Loading -> LoadingScreen()

                        is LoadState.Error -> ErrorScreen(
                            state.message,
                            detailsViewModel::retry
                        )

                        is LoadState.Success -> CharacterDetailsScreen(
                            character = state.data,
                            favoritesViewModel = favoritesViewModel,
                            savedCopy = false
                        )
                    }
                }
            }

            composable("planets") {
                PlanetsScreen()
            }

            composable("favorites") {
                FavoritesScreen(favoritesViewModel) { character ->
                    CharacterDetailsScreen(
                        character = character,
                        favoritesViewModel = favoritesViewModel,
                        savedCopy = true
                    )
                }
            }

            composable("settings") {
                FiltersScreen(
                    viewModel = filtersViewModel,
                    onDone = {
                        openTab("people")
                        // При сохранении тех же фильтров
                        // тоже обновляем список.
                        viewModel.retry()
                    }
                )
            }
        }
    }
}

@Composable
private fun CharacterListScreen(
    viewModel: CharactersViewModel,
    favoritesViewModel: FavoritesViewModel,
    onOpenFilters: () -> Unit,
    onCharacterClick: (Int) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val badgeVisible by viewModel.badgeVisible.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 16.dp),
            label = { Text("Имя на английском") },
            enabled = !viewModel.savingSearch,
            singleLine = true
        )

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.search()
                },
                enabled = !viewModel.savingSearch
            ) {
                Text("Найти")
            }

            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.clearSearch()
                },
                enabled = !viewModel.savingSearch
            ) {
                Text("Очистить")
            }

            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.retry()
                },
                enabled = viewModel.state !is LoadState.Loading &&
                        !viewModel.savingSearch
            ) {
                Text("Обновить")
            }
        }

        TextButton(onClick = onOpenFilters) {
            Text(
                if (badgeVisible) {
                    "Фильтры применены • Изменить"
                } else {
                    "Настроить фильтры"
                }
            )
        }

        when (val state = viewModel.state) {
            LoadState.Loading -> LoadingScreen()

            is LoadState.Error -> ErrorScreen(
                state.message,
                viewModel::retry
            )

            is LoadState.Success -> {
                val characters = state.data.characters

                Text(
                    "Показано: ${characters.size}",
                    Modifier.padding(horizontal = 16.dp)
                )

                if (characters.isEmpty()) {
                    Box(
                        Modifier.fillMaxWidth().weight(1f).padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Ничего не найдено.\nИзмените настройки поиска.",
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(characters, key = { it.id }) { character ->
                            Card(
                                onClick = {
                                    onCharacterClick(character.id)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CharacterAvatar(character)

                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            character.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Рост: ${measurement(character.height, "см")}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    FavoriteButton(
                                        character,
                                        favoritesViewModel,
                                        compact = true
                                    )
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
private fun FavoriteButton(
    character: StarWarsCharacter,
    viewModel: FavoritesViewModel,
    compact: Boolean = false
) {
    val favorite = viewModel.isFavorite(character.id)
    val ready = viewModel.state is LoadState.Success
    val pending = character.id in viewModel.pendingIds

    TextButton(
        onClick = {
            viewModel.setFavorite(character, !favorite)
        },
        enabled = ready && !pending
    ) {
        Text(
            when {
                pending -> "…"
                compact -> if (favorite) "★" else "☆"
                favorite -> "★ Удалить из избранного"
                else -> "☆ Добавить в избранное"
            }
        )
    }
}

@Composable
private fun CharacterAvatar(
    character: StarWarsCharacter,
    size: Dp = 64.dp
) {
    val resource = characterImageResource(character.id)

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (resource != null) {
            Image(
                painter = painterResource(resource),
                contentDescription = character.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                "★",
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}

@Composable
private fun CharacterDetailsScreen(
    character: StarWarsCharacter,
    favoritesViewModel: FavoritesViewModel,
    savedCopy: Boolean
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (savedCopy) {
            Text(
                "Сохранённая копия • Доступна без интернета",
                style = MaterialTheme.typography.labelLarge
            )
        }

        ConstraintLayout(Modifier.fillMaxWidth()) {
            val (avatar, name, label, height, mass, more) = createRefs()
            val middle = createGuidelineFromStart(0.5f)

            Box(
                Modifier.constrainAs(avatar) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                }
            ) {
                CharacterAvatar(character, 96.dp)
            }

            Text(
                character.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.constrainAs(name) {
                    start.linkTo(avatar.end, 16.dp)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    width = Dimension.fillToConstraints
                }
            )

            Text(
                "Досье №${character.id}",
                modifier = Modifier.constrainAs(label) {
                    start.linkTo(name.start)
                    end.linkTo(parent.end)
                    top.linkTo(name.bottom, 8.dp)
                    width = Dimension.fillToConstraints
                }
            )

            val headerBottom = createBottomBarrier(avatar, name, label)

            InfoCard(
                "Рост",
                measurement(character.height, "см"),
                Modifier.constrainAs(height) {
                    start.linkTo(parent.start)
                    end.linkTo(middle, 6.dp)
                    top.linkTo(headerBottom, 20.dp)
                    width = Dimension.fillToConstraints
                }
            )

            InfoCard(
                "Масса",
                measurement(character.mass, "кг"),
                Modifier.constrainAs(mass) {
                    start.linkTo(middle, 6.dp)
                    end.linkTo(parent.end)
                    top.linkTo(headerBottom, 20.dp)
                    width = Dimension.fillToConstraints
                }
            )

            val rowBottom = createBottomBarrier(height, mass)

            Column(
                modifier = Modifier.constrainAs(more) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(rowBottom, 12.dp)
                    width = Dimension.fillToConstraints
                },
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard("Год рождения", character.birthYear)
                InfoCard("Пол", character.gender)
                InfoCard("Цвет глаз", character.eyeColor)
                InfoCard("Цвет волос", character.hairColor)
                InfoCard("Цвет кожи", character.skinColor)
                InfoCard("Фильмов в SWAPI", character.filmCount.toString())
            }
        }

        FavoriteButton(
            character,
            favoritesViewModel
        )

        Text(
            "BBY — до битвы при Явине.\n" +
                    "ABY — после битвы при Явине.\n\n" +
                    "Данные: swapi.dev.\n" +
                    "Изображения: Wookieepedia / Fandom, Lucasfilm.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Card(modifier) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(value)
        }
    }
}

private fun measurement(value: String, unit: String): String {
    return if (value.replace(",", "").toDoubleOrNull() != null) {
        "$value $unit"
    } else {
        value
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Text(
            "Загрузка…",
            Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, textAlign = TextAlign.Center)

        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Повторить")
        }
    }
}