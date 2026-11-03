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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
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
    BottomTab("settings", "Настройки", "⚙"),
    BottomTab("profile", "Профиль", "●")
)

@Composable
fun StarWarsApp() {
    val appViewModel: StarWarsAppViewModel = composeViewModel(
        factory = starWarsAppViewModelFactory
    )

    val profileViewModel: ProfileViewModel = composeViewModel(
        factory = profileViewModelFactory
    )

    val badgeVisible by appViewModel.badgeVisible
        .collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val route = destination?.route

    val snackbarHostState = remember { SnackbarHostState() }

    val showError: suspend (String) -> Unit = { message ->
        snackbarHostState.showSnackbar(message)
        Unit
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
        "profile" -> "Профиль"
        "profile_edit" -> "Редактирование профиля"
        else -> "Архив галактики"
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(title)
                },
                navigationIcon = {
                    if (
                        route == "details/{characterId}" ||
                        route == "profile_edit"
                    ) {
                        TextButton(
                            onClick = {
                                navController.popBackStack()
                            },
                            enabled = route != "profile_edit" ||
                                    !profileViewModel.uiState.busy
                        ) {
                            Text("Назад")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (route != "profile_edit") {
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
                            label = {
                                Text(tab.title)
                            }
                        )
                    }
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
                    CharacterListRoute(
                        onOpenFilters = {
                            openTab("settings")
                        },
                        onCharacterClick = { id ->
                            navController.navigate("details/$id")
                        },
                        onShowError = showError
                    )
                }

                composable(
                    route = "details/{characterId}",
                    arguments = listOf(
                        navArgument("characterId") {
                            type = NavType.IntType
                        }
                    )
                ) {
                    CharacterDetailsRoute(
                        onShowError = showError
                    )
                }
            }

            composable("planets") {
                PlanetsScreen()
            }

            composable("favorites") {
                FavoritesRoute(
                    onShowError = showError
                )
            }

            composable("settings") {
                FiltersRoute(
                    onDone = {
                        openTab("people")
                    }
                )
            }

            composable("profile") {
                ProfileRoute(
                    viewModel = profileViewModel,
                    onEdit = {
                        navController.navigate("profile_edit") {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("profile_edit") {
                EditProfileRoute(
                    viewModel = profileViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun CharacterListRoute(
    onOpenFilters: () -> Unit,
    onCharacterClick: (Int) -> Unit,
    onShowError: suspend (String) -> Unit
) {
    val viewModel: CharactersViewModel = composeViewModel(
        factory = charactersViewModelFactory
    )

    val badgeVisible by viewModel.badgeVisible
        .collectAsStateWithLifecycle()

    val favoriteState = viewModel.favoriteUiState

    FavoriteErrorEffect(
        error = favoriteState.errorMessage,
        onDismiss = viewModel::dismissFavoriteError,
        onShowError = onShowError
    )

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onResume()
    }

    CharacterListScreen(
        state = viewModel.state,
        searchQuery = viewModel.searchQuery,
        savingSearch = viewModel.savingSearch,
        badgeVisible = badgeVisible,
        favoriteState = favoriteState,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onSearch = viewModel::search,
        onClearSearch = viewModel::clearSearch,
        onRetry = viewModel::retry,
        onFavoriteChange = viewModel::setFavorite,
        onOpenFilters = onOpenFilters,
        onCharacterClick = onCharacterClick
    )
}

@Composable
private fun CharacterDetailsRoute(
    onShowError: suspend (String) -> Unit
) {
    val viewModel: CharacterDetailsViewModel = composeViewModel(
        factory = characterDetailsViewModelFactory
    )

    val favoriteState = viewModel.favoriteUiState

    FavoriteErrorEffect(
        error = favoriteState.errorMessage,
        onDismiss = viewModel::dismissFavoriteError,
        onShowError = onShowError
    )

    when (val state = viewModel.state) {
        LoadState.Loading -> {
            LoadingScreen()
        }

        is LoadState.Error -> {
            ErrorScreen(
                message = state.message,
                onRetry = viewModel::retry
            )
        }

        is LoadState.Success -> {
            CharacterDetailsScreen(
                character = state.data,
                favoriteState = favoriteState,
                savedCopy = false,
                onFavoriteChange = viewModel::setFavorite
            )
        }
    }
}

@Composable
private fun FavoritesRoute(
    onShowError: suspend (String) -> Unit
) {
    val viewModel: FavoritesViewModel = composeViewModel(
        factory = favoritesViewModelFactory
    )

    val favoriteState = viewModel.favoriteUiState

    FavoriteErrorEffect(
        error = favoriteState.errorMessage,
        onDismiss = viewModel::dismissFavoriteError,
        onShowError = onShowError
    )

    FavoritesScreen(
        state = viewModel.state,
        pendingIds = favoriteState.pendingIds,
        onRetry = viewModel::retry,
        onRemove = viewModel::remove,
        details = { character ->
            CharacterDetailsScreen(
                character = character,
                favoriteState = favoriteState,
                savedCopy = true,
                onFavoriteChange = { favorite ->
                    viewModel.setFavorite(
                        character.id,
                        favorite
                    )
                }
            )
        }
    )
}

@Composable
private fun FiltersRoute(
    onDone: () -> Unit
) {
    val viewModel: FiltersViewModel = composeViewModel(
        factory = filtersViewModelFactory
    )

    val currentOnDone by rememberUpdatedState(onDone)

    LaunchedEffect(viewModel) {
        viewModel.loadSaved()
    }

    FiltersScreen(
        state = viewModel.uiState,
        onNameChange = viewModel::updateName,
        onOnlyWithPhotoChange = viewModel::updateOnlyWithPhoto,
        onGenderChange = viewModel::updateGender,
        onMaxMassChange = viewModel::updateMaxMass,
        onSave = {
            viewModel.save {
                currentOnDone()
            }
        },
        onReset = viewModel::resetDraft
    )
}

@Composable
private fun FavoriteErrorEffect(
    error: String?,
    onDismiss: () -> Unit,
    onShowError: suspend (String) -> Unit
) {
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentOnShowError by rememberUpdatedState(onShowError)

    LaunchedEffect(error) {
        if (error != null) {
            currentOnShowError(error)
            currentOnDismiss()
        }
    }
}

@Composable
private fun CharacterListScreen(
    state: LoadState<CharacterListUiModel>,
    searchQuery: String,
    savingSearch: Boolean,
    badgeVisible: Boolean,
    favoriteState: FavoriteUiState,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onRetry: () -> Unit,
    onFavoriteChange: (Int, Boolean) -> Unit,
    onOpenFilters: () -> Unit,
    onCharacterClick: (Int) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp
                ),
            label = {
                Text("Имя на английском")
            },
            enabled = !savingSearch,
            singleLine = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSearch()
                },
                enabled = !savingSearch
            ) {
                Text("Найти")
            }

            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    onClearSearch()
                },
                enabled = !savingSearch
            ) {
                Text("Очистить")
            }

            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    onRetry()
                },
                enabled = state !is LoadState.Loading &&
                        !savingSearch
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

        when (state) {
            LoadState.Loading -> {
                LoadingScreen()
            }

            is LoadState.Error -> {
                ErrorScreen(
                    message = state.message,
                    onRetry = onRetry
                )
            }

            is LoadState.Success -> {
                val characters = state.data.characters

                Text(
                    text = state.data.countText,
                    modifier = Modifier.padding(
                        horizontal = 16.dp
                    )
                )

                if (characters.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ничего не найдено.\n" +
                                    "Измените настройки поиска.",
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(
                            12.dp
                        )
                    ) {
                        items(
                            items = characters,
                            key = { it.id }
                        ) { character ->
                            Card(
                                onClick = {
                                    onCharacterClick(character.id)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(
                                        12.dp
                                    )
                                ) {
                                    CharacterAvatar(character)

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = character.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = character.heightText,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    FavoriteButton(
                                        favorite = character.id in
                                                favoriteState.favoriteIds,
                                        ready = favoriteState.ready,
                                        pending = character.id in
                                                favoriteState.pendingIds,
                                        compact = true,
                                        onFavoriteChange = { favorite ->
                                            onFavoriteChange(
                                                character.id,
                                                favorite
                                            )
                                        }
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
    favorite: Boolean,
    ready: Boolean,
    pending: Boolean,
    onFavoriteChange: (Boolean) -> Unit,
    compact: Boolean = false
) {
    TextButton(
        onClick = {
            onFavoriteChange(!favorite)
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
    character: CharacterUiModel,
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
                text = "★",
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}

@Composable
private fun CharacterDetailsScreen(
    character: CharacterUiModel,
    favoriteState: FavoriteUiState,
    savedCopy: Boolean,
    onFavoriteChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (savedCopy) {
            Text(
                text = "Сохранённая копия • Доступна без интернета",
                style = MaterialTheme.typography.labelLarge
            )
        }

        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (avatar, name, label, height, mass, more) =
                createRefs()

            val middle = createGuidelineFromStart(0.5f)

            Box(
                modifier = Modifier.constrainAs(avatar) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                }
            ) {
                CharacterAvatar(
                    character = character,
                    size = 96.dp
                )
            }

            Text(
                text = character.name,
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
                text = character.dossierTitle,
                modifier = Modifier.constrainAs(label) {
                    start.linkTo(name.start)
                    end.linkTo(parent.end)
                    top.linkTo(name.bottom, 8.dp)
                    width = Dimension.fillToConstraints
                }
            )

            val headerBottom = createBottomBarrier(
                avatar,
                name,
                label
            )

            InfoCard(
                title = "Рост",
                value = character.heightValue,
                modifier = Modifier.constrainAs(height) {
                    start.linkTo(parent.start)
                    end.linkTo(middle, 6.dp)
                    top.linkTo(headerBottom, 20.dp)
                    width = Dimension.fillToConstraints
                }
            )

            InfoCard(
                title = "Масса",
                value = character.massValue,
                modifier = Modifier.constrainAs(mass) {
                    start.linkTo(middle, 6.dp)
                    end.linkTo(parent.end)
                    top.linkTo(headerBottom, 20.dp)
                    width = Dimension.fillToConstraints
                }
            )

            val rowBottom = createBottomBarrier(
                height,
                mass
            )

            Column(
                modifier = Modifier.constrainAs(more) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(rowBottom, 12.dp)
                    width = Dimension.fillToConstraints
                },
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(
                    title = "Год рождения",
                    value = character.birthYear
                )

                InfoCard(
                    title = "Пол",
                    value = character.gender
                )

                InfoCard(
                    title = "Цвет глаз",
                    value = character.eyeColor
                )

                InfoCard(
                    title = "Цвет волос",
                    value = character.hairColor
                )

                InfoCard(
                    title = "Цвет кожи",
                    value = character.skinColor
                )

                InfoCard(
                    title = "Фильмов в SWAPI",
                    value = character.filmCountText
                )
            }
        }

        FavoriteButton(
            favorite = character.id in favoriteState.favoriteIds,
            ready = favoriteState.ready,
            pending = character.id in favoriteState.pendingIds,
            onFavoriteChange = onFavoriteChange
        )

        Text(
            text = "BBY — до битвы при Явине.\n" +
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
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(value)
        }
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()

        Text(
            text = "Загрузка…",
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun ErrorScreen(
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
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Повторить")
        }
    }
}