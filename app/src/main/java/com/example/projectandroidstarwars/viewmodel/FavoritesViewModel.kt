package com.example.projectandroidstarwars.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectandroidstarwars.domain.model.StarWarsCharacter
import com.example.projectandroidstarwars.domain.repository.FavoritesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: FavoritesRepository
) : ViewModel() {

    var state by mutableStateOf<LoadState<List<StarWarsCharacter>>>(
        LoadState.Loading
    )
        private set

    var pendingIds by mutableStateOf<Set<Int>>(emptySet())
        private set

    var actionError by mutableStateOf<String?>(null)
        private set

    private var observeJob: Job? = null

    init {
        retry()
    }

    fun retry() {
        observeJob?.cancel()
        state = LoadState.Loading

        observeJob = viewModelScope.launch {
            try {
                repository.observeFavorites().collect { characters ->
                    state = LoadState.Success(characters)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state = LoadState.Error(
                    "Не удалось прочитать избранное."
                )
            }
        }
    }

    fun isFavorite(id: Int): Boolean {
        val currentState = state

        return currentState is LoadState.Success &&
                currentState.data.any { it.id == id }
    }

    fun setFavorite(
        character: StarWarsCharacter,
        favorite: Boolean
    ) {
        if (character.id in pendingIds) return

        pendingIds = pendingIds + character.id
        actionError = null

        viewModelScope.launch {
            try {
                if (favorite) {
                    repository.save(character)
                } else {
                    repository.remove(character.id)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                actionError = "Не удалось изменить избранное."
            } finally {
                pendingIds = pendingIds - character.id
            }
        }
    }

    fun dismissError() {
        actionError = null
    }
}