package com.example.projectandroidstarwars.viewmodel

import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.projectandroidstarwars.di.AppContainer

val charactersViewModelFactory = viewModelFactory {
    initializer {
        CharactersViewModel(
            savedStateHandle = createSavedStateHandle(),
            getCharacters = AppContainer.getCharacters
        )
    }
}

val characterDetailsViewModelFactory = viewModelFactory {
    initializer {
        CharacterDetailsViewModel(
            savedStateHandle = createSavedStateHandle(),
            getCharacter = AppContainer.getCharacter
        )
    }
}