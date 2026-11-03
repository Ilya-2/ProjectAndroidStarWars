package com.example.projectandroidstarwars.di

import com.example.projectandroidstarwars.data.planets.NetworkPlanetRepository
import com.example.projectandroidstarwars.data.planets.PlanetApi
import com.example.projectandroidstarwars.domain.planets.GetPlanetUseCase
import com.example.projectandroidstarwars.domain.planets.GetPlanetsUseCase

object PlanetContainer {

    private val api: PlanetApi by lazy {
        NetworkContainer.retrofit.create(
            PlanetApi::class.java
        )
    }

    private val repository: NetworkPlanetRepository by lazy {
        NetworkPlanetRepository(api)
    }

    val getPlanets: GetPlanetsUseCase by lazy {
        GetPlanetsUseCase(repository)
    }

    val getPlanet: GetPlanetUseCase by lazy {
        GetPlanetUseCase(repository)
    }
}