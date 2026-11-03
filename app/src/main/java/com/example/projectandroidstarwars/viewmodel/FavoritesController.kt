package com.example.projectandroidstarwars.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.projectandroidstarwars.domain.model.StarWarsCharacter
import com.example.projectandroidstarwars.domain.repository.FavoritesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect

internal class FavoritesController(
    private val repository: FavoritesRepository,
    private val scope: CoroutineScope
) {

    var state by mutableStateOf<LoadState<List<CharacterUiModel>>>(
        LoadState.Loading
    )
        private set

    private var pendingIds by mutableStateOf<Set<Int>>(emptySet())
    private var actionError by mutableStateOf<String?>(null)

    private var charactersById: Map<Int, StarWarsCharacter> = emptyMap()
    private var observeJob: Job? = null

    val uiState: FavoriteUiState
        get() {
            val currentState = state

            val favoriteIds = if (currentState is LoadState.Success) {
                currentState.data.mapTo(mutableSetOf()) { it.id }
            } else {
                emptySet()
            }

            return FavoriteUiState(
                favoriteIds = favoriteIds,
                pendingIds = pendingIds,
                ready = currentState is LoadState.Success,
                errorMessage = actionError
            )
        }

    init {
        retry()
    }

    fun retry() {
        observeJob?.cancel()
        state = LoadState.Loading

        observeJob = scope.launchWithErrorHandling(
            defaultMessage = "Не удалось прочитать избранное.",
            onError = { message ->
                charactersById = emptyMap()
                state = LoadState.Error(message)
            }
        ) {
            repository.observeFavorites().collect { characters ->
                charactersById = characters.associateBy { it.id }

                state = LoadState.Success(
                    characters.map { it.toUiModel() }
                )
            }
        }
    }

    fun character(id: Int): StarWarsCharacter? {
        return charactersById[id]
    }

    fun setFavorite(
        character: StarWarsCharacter,
        favorite: Boolean
    ) {
        change(character.id) {
            if (favorite) {
                repository.save(character)
            } else {
                repository.remove(character.id)
            }
        }
    }

    fun remove(id: Int) {
        change(id) {
            repository.remove(id)
        }
    }

    fun dismissError() {
        actionError = null
    }

    private fun change(
        id: Int,
        action: suspend () -> Unit
    ) {
        if (state !is LoadState.Success) return
        if (id in pendingIds) return

        pendingIds = pendingIds + id
        actionError = null

        scope.launchWithErrorHandling(
            defaultMessage = "Не удалось изменить избранное.",
            onError = { message ->
                actionError = message
            },
            onFinally = {
                pendingIds = pendingIds - id
            }
        ) {
            action()
        }
    }
}