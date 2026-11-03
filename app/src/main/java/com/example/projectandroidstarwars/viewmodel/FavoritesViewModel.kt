package com.example.projectandroidstarwars.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectandroidstarwars.domain.repository.FavoritesRepository

class FavoritesViewModel(
    repository: FavoritesRepository
) : ViewModel() {

    private val favorites = FavoritesController(
        repository = repository,
        scope = viewModelScope
    )

    val state: LoadState<List<CharacterUiModel>>
        get() = favorites.state

    val favoriteUiState: FavoriteUiState
        get() = favorites.uiState

    fun retry() {
        favorites.retry()
    }

    fun remove(id: Int) {
        favorites.remove(id)
    }

    fun setFavorite(
        id: Int,
        favorite: Boolean
    ) {
        val character = favorites.character(id) ?: return
        favorites.setFavorite(character, favorite)
    }

    fun dismissFavoriteError() {
        favorites.dismissError()
    }
}