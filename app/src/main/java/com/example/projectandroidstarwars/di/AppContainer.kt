package com.example.projectandroidstarwars.di

import android.content.Context
import androidx.room.Room
import com.example.projectandroidstarwars.data.local.FilterBadgeCache
import com.example.projectandroidstarwars.data.local.PreferencesFiltersRepository
import com.example.projectandroidstarwars.data.local.StarWarsDatabase
import com.example.projectandroidstarwars.data.remote.SwapiApi
import com.example.projectandroidstarwars.data.repository.NetworkCharacterRepository
import com.example.projectandroidstarwars.data.repository.RoomFavoritesRepository
import com.example.projectandroidstarwars.domain.repository.FavoritesRepository
import com.example.projectandroidstarwars.domain.repository.FiltersRepository
import com.example.projectandroidstarwars.domain.usecase.GetCharacterUseCase
import com.example.projectandroidstarwars.domain.usecase.GetCharactersUseCase
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

    val filterBadgeCache by lazy {
        FilterBadgeCache(
            initialFilters = filtersRepository.filters.value
        )
    }

    private val database by lazy {
        Room.databaseBuilder(
            appContext,
            StarWarsDatabase::class.java,
            "star_wars.db"
        ).build()
    }

    val favoritesRepository: FavoritesRepository by lazy {
        RoomFavoritesRepository(database.favoritesDao())
    }

    private val api: SwapiApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://swapi.dev/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SwapiApi::class.java)
    }

    private val repository by lazy {
        NetworkCharacterRepository(api)
    }

    val getCharacters by lazy {
        GetCharactersUseCase(repository)
    }

    val getCharacter by lazy {
        GetCharacterUseCase(repository)
    }
}