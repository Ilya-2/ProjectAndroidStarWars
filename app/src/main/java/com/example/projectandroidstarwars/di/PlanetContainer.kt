package com.example.projectandroidstarwars.di

import com.example.projectandroidstarwars.data.planets.NetworkPlanetRepository
import com.example.projectandroidstarwars.data.planets.PlanetApi
import com.example.projectandroidstarwars.domain.planets.GetPlanetUseCase
import com.example.projectandroidstarwars.domain.planets.GetPlanetsUseCase
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PlanetContainer {

    private val api: PlanetApi by lazy {
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
            .create(PlanetApi::class.java)
    }

    private val repository by lazy {
        NetworkPlanetRepository(api)
    }

    val getPlanets by lazy {
        GetPlanetsUseCase(repository)
    }

    val getPlanet by lazy {
        GetPlanetUseCase(repository)
    }
}