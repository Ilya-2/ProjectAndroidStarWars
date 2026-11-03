package com.example.projectandroidstarwars.data.planets

import com.example.projectandroidstarwars.data.remote.NetworkRequestExecutor
import com.example.projectandroidstarwars.domain.planets.Planet
import com.example.projectandroidstarwars.domain.planets.PlanetLoadException
import com.example.projectandroidstarwars.domain.planets.PlanetRepository
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class PlanetListDto(
    val next: String? = null,
    val results: List<PlanetDto>? = null
)

data class PlanetDto(
    val url: String? = null,
    val name: String? = null,
    val climate: String? = null,
    val terrain: String? = null,
    val population: String? = null,
    val diameter: String? = null,
    val gravity: String? = null,

    @SerializedName("rotation_period")
    val rotationPeriod: String? = null,

    @SerializedName("orbital_period")
    val orbitalPeriod: String? = null,

    @SerializedName("surface_water")
    val surfaceWater: String? = null,

    val films: List<String>? = null
)

interface PlanetApi {

    @GET("planets/")
    suspend fun getPlanets(
        @Query("search") search: String?,
        @Query("page") page: Int
    ): PlanetListDto

    @GET("planets/{id}/")
    suspend fun getPlanet(
        @Path("id") id: Int
    ): PlanetDto
}

private fun String?.planetValue(): String {
    return when {
        this.isNullOrBlank() -> "Неизвестно"
        this == "unknown" -> "Неизвестно"
        this == "n/a" -> "Не применимо"
        else -> this
    }
}

private fun PlanetDto.toDomain(): Planet {
    val id = url
        ?.trimEnd('/')
        ?.substringAfterLast('/')
        ?.toIntOrNull()
        ?: throw IllegalArgumentException("Отсутствует ID планеты")

    require(id > 0 && !name.isNullOrBlank())

    return Planet(
        id = id,
        name = name.orEmpty(),
        climate = climate.planetValue(),
        terrain = terrain.planetValue(),
        population = population.planetValue(),
        diameter = diameter.planetValue(),
        gravity = gravity.planetValue(),
        rotationPeriod = rotationPeriod.planetValue(),
        orbitalPeriod = orbitalPeriod.planetValue(),
        surfaceWater = surfaceWater.planetValue(),
        filmCount = films?.size ?: 0
    )
}

class NetworkPlanetRepository(
    private val api: PlanetApi,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PlanetRepository {

    private val requests = NetworkRequestExecutor(
        createException = ::PlanetLoadException,
        notFoundMessage = "Планета не найдена.",
        ioDispatcher = ioDispatcher
    )

    override suspend fun getPlanets(
        search: String
    ): List<Planet> = requests.execute {
        val response = api.getPlanets(
            search = search.trim().takeIf { it.isNotEmpty() },
            page = 1
        )

        val results = requireNotNull(response.results) {
            "Отсутствует список планет"
        }

        results
            .take(MAX_PLANETS)
            .map { it.toDomain() }
    }

    override suspend fun getPlanet(
        id: Int
    ): Planet = requests.execute {
        require(id > 0)
        api.getPlanet(id).toDomain()
    }

    companion object {
        private const val MAX_PLANETS = 5
    }
}