package com.example.projectandroidstarwars.di

import android.content.Context
import androidx.room.Room
import com.example.projectandroidstarwars.data.local.PreferencesFiltersRepository
import com.example.projectandroidstarwars.data.local.StarWarsDatabase
import com.example.projectandroidstarwars.data.remote.SwapiApi
import com.example.projectandroidstarwars.data.repository.FileProfileRepository
import com.example.projectandroidstarwars.data.repository.NetworkCharacterRepository
import com.example.projectandroidstarwars.data.repository.RoomFavoritesRepository
import com.example.projectandroidstarwars.domain.repository.FavoritesRepository
import com.example.projectandroidstarwars.domain.repository.FiltersRepository
import com.example.projectandroidstarwars.domain.repository.ProfileRepository
import com.example.projectandroidstarwars.domain.state.FilterBadgeCache
import com.example.projectandroidstarwars.domain.usecase.GetCharacterUseCase
import com.example.projectandroidstarwars.domain.usecase.GetCharactersUseCase

object AppContainer {

    private lateinit var appContext: Context

    @Synchronized
    fun initialize(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
        }
    }

    val filtersRepository: FiltersRepository by lazy {
        PreferencesFiltersRepository(appContext)
    }

    val filterBadgeCache: FilterBadgeCache by lazy {
        FilterBadgeCache(
            initialFilters = filtersRepository.filters.value
        )
    }

    private val database: StarWarsDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            StarWarsDatabase::class.java,
            "star_wars.db"
        ).build()
    }

    val favoritesRepository: FavoritesRepository by lazy {
        RoomFavoritesRepository(
            database.favoritesDao()
        )
    }

    val profileRepository: ProfileRepository by lazy {
        FileProfileRepository(appContext)
    }

    private val api: SwapiApi by lazy {
        NetworkContainer.retrofit.create(
            SwapiApi::class.java
        )
    }

    private val repository: NetworkCharacterRepository by lazy {
        NetworkCharacterRepository(api)
    }

    val getCharacters: GetCharactersUseCase by lazy {
        GetCharactersUseCase(repository)
    }

    val getCharacter: GetCharacterUseCase by lazy {
        GetCharacterUseCase(repository)
    }
}