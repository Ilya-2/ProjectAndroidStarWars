package com.example.projectandroidstarwars.ui.theme

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projectandroidstarwars.domain.model.StarWarsCharacter
import com.example.projectandroidstarwars.viewmodel.FavoritesViewModel
import com.example.projectandroidstarwars.viewmodel.LoadState

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    details: @Composable (StarWarsCharacter) -> Unit
) {
    var selectedId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    BackHandler(enabled = selectedId != null) {
        selectedId = null
    }

    Column(Modifier.fillMaxSize()) {
        if (selectedId != null) {
            TextButton(onClick = { selectedId = null }) {
                Text("← К избранному")
            }
        }

        when (val state = viewModel.state) {
            LoadState.Loading -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is LoadState.Error -> {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(state.message)

                    Button(onClick = viewModel::retry) {
                        Text("Повторить")
                    }
                }
            }

            is LoadState.Success -> {
                val selected = state.data.firstOrNull {
                    it.id == selectedId
                }

                if (selectedId != null) {
                    if (selected != null) {
                        details(selected)
                    } else {
                        Text(
                            "Персонаж удалён из избранного.",
                            Modifier.padding(16.dp)
                        )
                    }
                } else if (state.data.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Избранное пока пустое.\n" +
                                    "Нажми ☆ рядом с персонажем."
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.data,
                            key = { it.id }
                        ) { character ->
                            Card(
                                onClick = { selectedId = character.id },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            character.name,
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Text(
                                            "Сохранён на устройстве",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            viewModel.setFavorite(
                                                character,
                                                false
                                            )
                                        },
                                        enabled = character.id !in viewModel.pendingIds
                                    ) {
                                        Text("Удалить")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}