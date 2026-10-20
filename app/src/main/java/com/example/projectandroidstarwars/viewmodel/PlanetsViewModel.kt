package com.example.projectandroidstarwars.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.projectandroidstarwars.di.PlanetContainer
import com.example.projectandroidstarwars.domain.planets.GetPlanetUseCase
import com.example.projectandroidstarwars.domain.planets.GetPlanetsUseCase
import com.example.projectandroidstarwars.domain.planets.Planet
import com.example.projectandroidstarwars.domain.planets.PlanetLoadException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PlanetsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getPlanets: GetPlanetsUseCase,
    private val getPlanet: GetPlanetUseCase
) : ViewModel() {

    var searchQuery by mutableStateOf(
        savedStateHandle.get<String>("planet_draft") ?: ""
    )
        private set

    private var activeQuery =
        savedStateHandle.get<String>("planet_query") ?: ""

    var selectedPlanetId by mutableStateOf(
        savedStateHandle.get<Int>("selected_planet")
    )
        private set

    var listState by mutableStateOf<LoadState<List<Planet>>>(
        LoadState.Loading
    )
        private set

    var detailsState by mutableStateOf<LoadState<Planet>>(
        LoadState.Loading
    )
        private set

    private var listJob: Job? = null
    private var detailsJob: Job? = null

    init {
        refresh()

        selectedPlanetId?.let { id ->
            openPlanet(id)
        }
    }

    fun updateSearchQuery(value: String) {
        searchQuery = value
        savedStateHandle["planet_draft"] = value
    }

    fun search() {
        activeQuery = searchQuery.trim()
        savedStateHandle["planet_query"] = activeQuery
        refresh()
    }

    fun clearSearch() {
        updateSearchQuery("")
        search()
    }

    fun refresh() {
        listJob?.cancel()
        listState = LoadState.Loading

        val query = activeQuery

        listJob = viewModelScope.launch {
            try {
                listState = LoadState.Success(
                    getPlanets(query)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: PlanetLoadException) {
                listState = LoadState.Error(
                    e.message ?: "Не удалось загрузить планеты."
                )
            } catch (e: Exception) {
                listState = LoadState.Error(
                    "Не удалось обработать список планет."
                )
            }
        }
    }

    fun openPlanet(id: Int) {
        detailsJob?.cancel()

        selectedPlanetId = id
        savedStateHandle["selected_planet"] = id
        detailsState = LoadState.Loading

        detailsJob = viewModelScope.launch {
            try {
                detailsState = LoadState.Success(
                    getPlanet(id)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: PlanetLoadException) {
                detailsState = LoadState.Error(
                    e.message ?: "Не удалось загрузить планету."
                )
            } catch (e: Exception) {
                detailsState = LoadState.Error(
                    "Не удалось обработать данные планеты."
                )
            }
        }
    }

    fun retryDetails() {
        selectedPlanetId?.let { id ->
            openPlanet(id)
        }
    }

    fun backToList() {
        detailsJob?.cancel()
        selectedPlanetId = null
        savedStateHandle.remove<Int>("selected_planet")
    }
}

val planetsViewModelFactory = viewModelFactory {
    initializer {
        PlanetsViewModel(
            savedStateHandle = createSavedStateHandle(),
            getPlanets = PlanetContainer.getPlanets,
            getPlanet = PlanetContainer.getPlanet
        )
    }
}