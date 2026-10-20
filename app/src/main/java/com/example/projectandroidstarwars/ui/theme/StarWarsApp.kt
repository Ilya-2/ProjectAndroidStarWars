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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projectandroidstarwars.domain.model.CharacterPage
import com.example.projectandroidstarwars.domain.model.StarWarsCharacter
import com.example.projectandroidstarwars.ui.characterImageResource
import com.example.projectandroidstarwars.viewmodel.CharacterDetailsViewModel
import com.example.projectandroidstarwars.viewmodel.CharactersViewModel
import com.example.projectandroidstarwars.viewmodel.LoadState
import com.example.projectandroidstarwars.viewmodel.characterDetailsViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel

private data class BottomTab(
    val route: String,
    val title: String,
    val symbol: String
)

private val bottomTabs = listOf(
    BottomTab("people", "Персонажи", "★"),
    BottomTab("planets", "Планеты", "◎"),
    BottomTab("settings", "Настройки", "⚙")
)

@Composable
fun StarWarsApp(viewModel: CharactersViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val route = destination?.route
    val isDetails = route == "details/{characterId}"

    val title = when (route) {
        "details/{characterId}" -> "Досье персонажа"
        "planets" -> "Планеты"
        "settings" -> "Настройки"
        else -> "Архив галактики"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (isDetails) {
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
                                navController.navigate(tab.route) {
                                    popUpTo(
                                        navController.graph
                                            .findStartDestination().id
                                    ) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Text(tab.symbol) },
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
                        state = viewModel.state,
                        searchQuery = viewModel.searchQuery,
                        onSearchChange = viewModel::updateSearchQuery,
                        onSearch = viewModel::search,
                        onClear = {
                            viewModel.updateSearchQuery("")
                            viewModel.search()
                        },
                        onRetry = viewModel::retry,
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

                        is LoadState.Success -> {
                            CharacterDetailsScreen(state.data)
                        }
                    }
                }
            }

            composable("planets") {
                PlanetsScreen()
            }

            composable("settings") {
                SettingsScreen()
            }
        }
    }
}

@Composable
private fun CharacterListScreen(
    state: LoadState<CharacterPage>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onRetry: () -> Unit,
    onCharacterClick: (Int) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 16.dp),
            label = { Text("Имя на английском, например Luke") },
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
                    onRetry()
                },
                enabled = state !is LoadState.Loading
            ) {
                Text("Обновить")
            }
        }

        when (state) {
            LoadState.Loading -> LoadingScreen()

            is LoadState.Error -> ErrorScreen(
                state.message,
                onRetry
            )

            is LoadState.Success -> {
                val page = state.data

                Text(
                    text = "Загружено: ${page.characters.size}",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.labelLarge
                )

                if (page.characters.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ничего не найдено.\n" +
                                    "Введите другое имя или нажмите «Очистить».",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    key(page.page, page.characters.first().id) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = page.characters,
                                key = { it.id }
                            ) { character ->
                                CharacterCard(character) {
                                    onCharacterClick(character.id)
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
private fun CharacterCard(
    character: StarWarsCharacter,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CharacterAvatar(character)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    character.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Рост: ${measurement(character.height, "см")}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    "Рождение: ${character.birthYear}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text("›", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun CharacterAvatar(
    character: StarWarsCharacter,
    size: Dp = 64.dp
) {
    val imageResource = characterImageResource(character.id)

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (imageResource != null) {
            Image(
                painter = painterResource(imageResource),
                contentDescription = character.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                "★",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CharacterDetailsScreen(character: StarWarsCharacter) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ConstraintLayout(Modifier.fillMaxWidth()) {
            val (avatar, name, label) = createRefs()
            val (height, mass, birthYear, eyeColor) = createRefs()
            val (gender, heading, description, note) = createRefs()

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
                    start.linkTo(avatar.end, margin = 16.dp)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    width = Dimension.fillToConstraints
                }
            )

            Text(
                "STAR WARS • ДОСЬЕ №${character.id}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.constrainAs(label) {
                    start.linkTo(name.start)
                    end.linkTo(parent.end)
                    top.linkTo(name.bottom, margin = 8.dp)
                    width = Dimension.fillToConstraints
                }
            )

            val headerBottom = createBottomBarrier(avatar, name, label)

            InfoCard(
                "Рост",
                measurement(character.height, "см"),
                Modifier.constrainAs(height) {
                    start.linkTo(parent.start)
                    end.linkTo(middle, margin = 6.dp)
                    top.linkTo(headerBottom, margin = 24.dp)
                    width = Dimension.fillToConstraints
                }
            )

            InfoCard(
                "Масса",
                measurement(character.mass, "кг"),
                Modifier.constrainAs(mass) {
                    start.linkTo(middle, margin = 6.dp)
                    end.linkTo(parent.end)
                    top.linkTo(headerBottom, margin = 24.dp)
                    width = Dimension.fillToConstraints
                }
            )

            val firstRowBottom = createBottomBarrier(height, mass)

            InfoCard(
                "Год рождения",
                character.birthYear,
                Modifier.constrainAs(birthYear) {
                    start.linkTo(parent.start)
                    end.linkTo(middle, margin = 6.dp)
                    top.linkTo(firstRowBottom, margin = 12.dp)
                    width = Dimension.fillToConstraints
                }
            )

            InfoCard(
                "Цвет глаз",
                character.eyeColor,
                Modifier.constrainAs(eyeColor) {
                    start.linkTo(middle, margin = 6.dp)
                    end.linkTo(parent.end)
                    top.linkTo(firstRowBottom, margin = 12.dp)
                    width = Dimension.fillToConstraints
                }
            )

            val secondRowBottom = createBottomBarrier(birthYear, eyeColor)

            InfoCard(
                "Пол",
                character.gender,
                Modifier.constrainAs(gender) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(secondRowBottom, margin = 12.dp)
                    width = Dimension.fillToConstraints
                }
            )

            Text(
                "Дополнительные сведения",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.constrainAs(heading) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(gender.bottom, margin = 24.dp)
                    width = Dimension.fillToConstraints
                }
            )

            Text(
                "Цвет волос: ${character.hairColor}\n" +
                        "Цвет кожи: ${character.skinColor}\n" +
                        "Фильмов в SWAPI: ${character.filmCount}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.constrainAs(description) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(heading.bottom, margin = 12.dp)
                    width = Dimension.fillToConstraints
                }
            )

            Text(
                "BBY — до битвы при Явине.\n" +
                        "ABY — после битвы при Явине.\n\n" +
                        "Данные: swapi.dev.\n" +
                        "Изображения: Wookieepedia / Fandom, Lucasfilm.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.constrainAs(note) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(description.bottom, margin = 20.dp)
                    width = Dimension.fillToConstraints
                }
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
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
            "Загружаем данные из SWAPI…",
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
        Text(
            "Не удалось загрузить данные",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            message,
            Modifier.padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text("Повторить")
        }
    }
}

@Composable
private fun SettingsScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "⚙",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Настройки приложения",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            "Персонажи и планеты загружаются из SWAPI. " +
                    "Фотографии сохранены в приложении.",
            textAlign = TextAlign.Center
        )
    }
}