package com.example.projectandroidstarwars.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.projectandroidstarwars.di.AppContainer
import com.example.projectandroidstarwars.notifications.LessonReminderScheduler
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
            },
            favoritesRepository = AppContainer.favoritesRepository
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
            getCharacter = AppContainer.getCharacter,
            favoritesRepository = AppContainer.favoritesRepository
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

val profileViewModelFactory = viewModelFactory {
    initializer {
        val application = checkNotNull(this[APPLICATION_KEY])

        AppContainer.initialize(application)

        ProfileViewModel(
            repository = AppContainer.profileRepository,
            savedStateHandle = createSavedStateHandle(),
            reminderScheduler = LessonReminderScheduler(application)
        )
    }
}