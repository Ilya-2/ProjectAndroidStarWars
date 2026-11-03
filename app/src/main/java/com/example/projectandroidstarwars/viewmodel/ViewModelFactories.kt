package com.example.projectandroidstarwars.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.projectandroidstarwars.di.AppContainer
import com.example.projectandroidstarwars.ui.characterImageResource

val charactersViewModelFactory = viewModelFactory {
    initializer {
        AppContainer.initialize(
            checkNotNull(this[APPLICATION_KEY])
        )

        CharactersViewModel(
            getCharacters = AppContainer.getCharacters,
            filtersRepository = AppContainer.filtersRepository,
            badgeCache = AppContainer.filterBadgeCache,
            hasPhoto = { id ->
                characterImageResource(id) != null
            }
        )
    }
}

val characterDetailsViewModelFactory = viewModelFactory {
    initializer {
        AppContainer.initialize(
            checkNotNull(this[APPLICATION_KEY])
        )

        CharacterDetailsViewModel(
            savedStateHandle = createSavedStateHandle(),
            getCharacter = AppContainer.getCharacter
        )
    }
}

val filtersViewModelFactory = viewModelFactory {
    initializer {
        AppContainer.initialize(
            checkNotNull(this[APPLICATION_KEY])
        )

        FiltersViewModel(
            repository = AppContainer.filtersRepository,
            badgeCache = AppContainer.filterBadgeCache
        )
    }
}

val favoritesViewModelFactory = viewModelFactory {
    initializer {
        AppContainer.initialize(
            checkNotNull(this[APPLICATION_KEY])
        )

        FavoritesViewModel(
            repository = AppContainer.favoritesRepository
        )
    }
}