@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.projectandroidstarwars.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import com.example.projectandroidstarwars.ui.characterImageResource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
import com.example.projectandroidstarwars.model.StarWarsCharacter
import com.example.projectandroidstarwars.viewmodel.CharactersViewModel

private data class BottomTab(val route: String, val title: String, val symbol: String)

private val bottomTabs = listOf(
    BottomTab("people", "Персонажи", "★"),
    BottomTab("planets", "Планеты", "◎"),
    BottomTab("settings", "Настройки", "⚙")
)

@Composable
fun StarWarsApp(viewModel: CharactersViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = currentDestination?.route
    val isDetails = currentRoute == "details/{characterId}"
    val screenTitle = when (currentRoute) {
        "details/{characterId}" -> "Досье персонажа"
        "planets" -> "Планеты"
        "settings" -> "Настройки"
        else -> "Архив галактики"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    if (isDetails) {
                        TextButton(onClick = { navController.popBackStack() }) {
                            Text("Назад")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                bottomTabs.forEach { tab ->
                    val isSelected = currentDestination?.hierarchy?.any {
                        it.route == tab.route
                    } == true
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
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
            modifier = Modifier.padding(innerPadding)
                .consumeWindowInsets(innerPadding).fillMaxSize()
        ) {
            navigation(startDestination = "characters", route = "people") {
                composable("characters") {
                    CharacterListScreen(
                        characters = viewModel.characters,
                        searchQuery = viewModel.searchQuery,
                        onSearchChange = { viewModel.updateSearchQuery(it) },
                        onCharacterClick = { navController.navigate("details/$it") }
                    )
                }
                composable(
                    route = "details/{characterId}",
                    arguments = listOf(navArgument("characterId") { type = NavType.IntType })
                ) { entry ->
                    val id = entry.arguments?.getInt("characterId") ?: -1
                    CharacterDetailsScreen(viewModel.getCharacterById(id))
                }
            }
            composable("planets") {
                PlaceholderScreen(
                    "Планеты галактики", "◎",
                    "Здесь появится каталог планет: Татуин, Набу, Корусант и другие миры. " +
                            "Раздел будет реализован в следующих практиках."
                )
            }
            composable("settings") {
                PlaceholderScreen(
                    "Настройки приложения", "⚙",
                    "Здесь появятся настройки приложения. Пока используются локальные " +
                            "учебные данные. Фотографии тоже находятся в приложении, интернет не нужен."
                )
            }
        }
    }
}

@Composable
private fun CharacterListScreen(
    characters: List<StarWarsCharacter>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCharacterClick: (Int) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, end = 16.dp),
            label = { Text("Поиск персонажа") },
            singleLine = true
        )
        Text(
            "Найдено персонажей: ${characters.size}",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.labelLarge
        )
        if (characters.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Ничего не найдено.\nПопробуйте другое имя.", textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(characters, key = { it.id }) { character ->
                    CharacterCard(character) { onCharacterClick(character.id) }
                }
            }
        }
    }
}

@Composable
private fun CharacterCard(character: StarWarsCharacter, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CharacterAvatar(character)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(character.name, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Text("Рост: ${character.height} см", style = MaterialTheme.typography.bodyMedium)
                Text("Рождение: ${character.birthYear}", style = MaterialTheme.typography.bodySmall)
            }
            Text("›", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun CharacterAvatar(character: StarWarsCharacter, size: Dp = 64.dp) {
    Image(
        painter = painterResource(characterImageResource(character.id)),
        contentDescription = character.name,
        // Показываем портрет целиком: у дроидов и Чубакки важен весь силуэт.
        contentScale = ContentScale.Fit,
        modifier = Modifier.size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
private fun CharacterDetailsScreen(character: StarWarsCharacter?) {
    if (character == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Персонаж не найден")
        }
        return
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        ConstraintLayout(Modifier.fillMaxWidth()) {
            val (avatar, name, label) = createRefs()
            val (height, mass, birthYear, eyeColor) = createRefs()
            val (gender, heading, description, note) = createRefs()
            val middle = createGuidelineFromStart(0.5f)

            Box(Modifier.constrainAs(avatar) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
            }) { CharacterAvatar(character, size = 96.dp) }

            Text(character.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.constrainAs(name) {
                    start.linkTo(avatar.end, margin = 16.dp)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    width = Dimension.fillToConstraints
                })
            Text("STAR WARS • ДОСЬЕ №${character.id}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.constrainAs(label) {
                    start.linkTo(name.start)
                    end.linkTo(parent.end)
                    top.linkTo(name.bottom, margin = 8.dp)
                    width = Dimension.fillToConstraints
                })

            val headerBottom = createBottomBarrier(avatar, name, label)
            InfoCard("Рост", "${character.height} см", Modifier.constrainAs(height) {
                start.linkTo(parent.start)
                end.linkTo(middle, margin = 6.dp)
                top.linkTo(headerBottom, margin = 24.dp)
                width = Dimension.fillToConstraints
            })
            InfoCard("Масса", "${character.mass} кг", Modifier.constrainAs(mass) {
                start.linkTo(middle, margin = 6.dp)
                end.linkTo(parent.end)
                top.linkTo(headerBottom, margin = 24.dp)
                width = Dimension.fillToConstraints
            })
            val firstRowBottom = createBottomBarrier(height, mass)
            InfoCard("Год рождения", character.birthYear, Modifier.constrainAs(birthYear) {
                start.linkTo(parent.start)
                end.linkTo(middle, margin = 6.dp)
                top.linkTo(firstRowBottom, margin = 12.dp)
                width = Dimension.fillToConstraints
            })
            InfoCard("Цвет глаз", character.eyeColor, Modifier.constrainAs(eyeColor) {
                start.linkTo(middle, margin = 6.dp)
                end.linkTo(parent.end)
                top.linkTo(firstRowBottom, margin = 12.dp)
                width = Dimension.fillToConstraints
            })
            val secondRowBottom = createBottomBarrier(birthYear, eyeColor)
            InfoCard("Пол", character.gender, Modifier.constrainAs(gender) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(secondRowBottom, margin = 12.dp)
                width = Dimension.fillToConstraints
            })
            Text("О персонаже", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.constrainAs(heading) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(gender.bottom, margin = 24.dp)
                    width = Dimension.fillToConstraints
                })
            Text(character.description, style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.constrainAs(description) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(heading.bottom, margin = 12.dp)
                    width = Dimension.fillToConstraints
                })
            Text("BBY — до битвы при Явине.\nABY — после битвы при Явине.\n\n" +
                    "Учебные данные. Изображения: Wookieepedia / Fandom, Lucasfilm.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.constrainAs(note) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(description.bottom, margin = 20.dp)
                    width = Dimension.fillToConstraints
                })
        }
    }
}

@Composable
private fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, symbol: String, description: String) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(symbol, style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(description, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}
