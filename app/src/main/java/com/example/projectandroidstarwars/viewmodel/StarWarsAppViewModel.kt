package com.example.projectandroidstarwars.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.projectandroidstarwars.di.AppContainer
import com.example.projectandroidstarwars.domain.state.FilterBadgeCache
import kotlinx.coroutines.flow.StateFlow

class StarWarsAppViewModel(
    badgeCache: FilterBadgeCache
) : ViewModel() {

    val badgeVisible: StateFlow<Boolean> = badgeCache.isVisible
}

val starWarsAppViewModelFactory = viewModelFactory {
    initializer {
        AppContainer.initialize(
            checkNotNull(this[APPLICATION_KEY])
        )

        StarWarsAppViewModel(
            badgeCache = AppContainer.filterBadgeCache
        )
    }
}