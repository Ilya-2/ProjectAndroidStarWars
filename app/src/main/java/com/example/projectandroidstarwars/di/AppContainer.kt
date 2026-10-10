package com.example.projectandroidstarwars.di

import com.example.projectandroidstarwars.data.remote.SwapiApi
import com.example.projectandroidstarwars.data.repository.NetworkCharacterRepository
import com.example.projectandroidstarwars.domain.usecase.GetCharacterUseCase
import com.example.projectandroidstarwars.domain.usecase.GetCharactersUseCase
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppContainer {

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