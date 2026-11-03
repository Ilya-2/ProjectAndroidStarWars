package com.example.projectandroidstarwars.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectandroidstarwars.domain.model.StarWarsCharacter
import com.example.projectandroidstarwars.domain.repository.FavoritesRepository
import com.example.projectandroidstarwars.domain.usecase.GetCharacterUseCase
import kotlinx.coroutines.Job

class CharacterDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getCharacter: GetCharacterUseCase,
    favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val favorites = FavoritesController(
        repository = favoritesRepository,
        scope = viewModelScope
    )

    private val id: Int =
        savedStateHandle.get<Int>("characterId") ?: -1

    private var job: Job? = null
    private var loadedCharacter: StarWarsCharacter? = null

    var state by mutableStateOf<LoadState<CharacterUiModel>>(
        LoadState.Loading
    )
        private set

    val favoriteUiState: FavoriteUiState
        get() = favorites.uiState

    init {
        retry()
    }

    fun retry() {
        job?.cancel()
        loadedCharacter = null

        if (id <= 0) {
            state = LoadState.Error(
                "Некорректный номер персонажа."
            )
            return
        }

        state = LoadState.Loading

        job = viewModelScope.launchWithErrorHandling(
            defaultMessage = "Не удалось обработать досье. Повторите попытку.",
            onError = { message ->
                state = LoadState.Error(message)
            }
        ) {
            val character = getCharacter(id)
            loadedCharacter = character

            state = LoadState.Success(
                character.toUiModel()
            )
        }
    }

    fun setFavorite(favorite: Boolean) {
        val character = loadedCharacter ?: return
        favorites.setFavorite(character, favorite)
    }

    fun dismissFavoriteError() {
        favorites.dismissError()
    }
}