package com.example.projectandroidstarwars.domain.planets

data class Planet(
    val id: Int,
    val name: String,
    val climate: String,
    val terrain: String,
    val population: String,
    val diameter: String,
    val gravity: String,
    val rotationPeriod: String,
    val orbitalPeriod: String,
    val surfaceWater: String,
    val filmCount: Int
)

interface PlanetRepository {
    suspend fun getPlanets(search: String): List<Planet>
    suspend fun getPlanet(id: Int): Planet
}

class GetPlanetsUseCase(
    private val repository: PlanetRepository
) {
    suspend operator fun invoke(search: String): List<Planet> {
        return repository.getPlanets(search.trim())
    }
}

class GetPlanetUseCase(
    private val repository: PlanetRepository
) {
    suspend operator fun invoke(id: Int): Planet {
        return repository.getPlanet(id)
    }
}

class PlanetLoadException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)