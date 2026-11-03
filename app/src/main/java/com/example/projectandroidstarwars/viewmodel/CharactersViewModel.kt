package com.example.projectandroidstarwars.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectandroidstarwars.data.local.FilterBadgeCache
import com.example.projectandroidstarwars.domain.model.CharacterFilters
import com.example.projectandroidstarwars.domain.model.CharacterPage
import com.example.projectandroidstarwars.domain.model.GenderFilter
import com.example.projectandroidstarwars.domain.model.StarWarsCharacter
import com.example.projectandroidstarwars.domain.repository.CharacterLoadException
import com.example.projectandroidstarwars.domain.repository.FiltersRepository
import com.example.projectandroidstarwars.domain.usecase.GetCharactersUseCase
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CharactersViewModel(
    private val getCharacters: GetCharactersUseCase,
    private val filtersRepository: FiltersRepository,
    private val badgeCache: FilterBadgeCache,
    private val hasPhoto: (Int) -> Boolean
) : ViewModel() {

    var searchQuery by mutableStateOf(
        filtersRepository.filters.value.name
    )
        private set

    var state by mutableStateOf<LoadState<CharacterPage>>(
        LoadState.Loading
    )
        private set

    var savingSearch by mutableStateOf(false)
        private set

    val badgeVisible = badgeCache.isVisible

    private var requestJob: Job? = null

    init {
        viewModelScope.launch {
            filtersRepository.filters.collect { filters ->
                searchQuery = filters.name
                badgeCache.update(filters)
                load(filters)
            }
        }
    }

    fun updateSearchQuery(value: String) {
        searchQuery = value
    }

    fun search() {
        if (savingSearch) return

        val oldFilters = filtersRepository.filters.value
        val newFilters = oldFilters.copy(
            name = searchQuery.trim()
        )

        savingSearch = true

        viewModelScope.launch {
            try {
                filtersRepository.save(newFilters)
                searchQuery = newFilters.name
                badgeCache.update(newFilters)

                if (oldFilters == newFilters) {
                    load(newFilters)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state = LoadState.Error(
                    "Не удалось сохранить поисковый запрос."
                )
            } finally {
                savingSearch = false
            }
        }
    }

    fun clearSearch() {
        if (savingSearch) return

        updateSearchQuery("")
        search()
    }

    fun retry() {
        load(filtersRepository.filters.value)
    }

    private fun load(filters: CharacterFilters) {
        requestJob?.cancel()
        state = LoadState.Loading

        requestJob = viewModelScope.launch {
            try {
                val page = getCharacters(
                    filters.name,
                    1
                )

                val filteredCharacters = page.characters.filter {
                    matchesFilters(it, filters)
                }

                state = LoadState.Success(
                    page.copy(
                        characters = filteredCharacters,
                        total = filteredCharacters.size,
                        page = 1,
                        hasNext = false
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: CharacterLoadException) {
                state = LoadState.Error(
                    e.message ?: "Не удалось загрузить персонажей."
                )
            } catch (e: Exception) {
                state = LoadState.Error(
                    "Не удалось обработать список персонажей."
                )
            }
        }
    }

    private fun matchesFilters(
        character: StarWarsCharacter,
        filters: CharacterFilters
    ): Boolean {
        val photoMatches =
            !filters.onlyWithPhoto || hasPhoto(character.id)

        val gender = character.gender
            .trim()
            .lowercase(Locale.ROOT)

        val genderMatches = when (filters.gender) {
            GenderFilter.ANY -> true
            GenderFilter.MALE -> gender == "male"
            GenderFilter.FEMALE -> gender == "female"

            GenderFilter.NOT_APPLICABLE -> {
                gender == "n/a" || gender == "не применимо"
            }
        }

        val maximum = filters.maxMass

        val massMatches = if (maximum == null) {
            true
        } else {
            // В SWAPI "1,358" означает 1358 кг.
            val mass = character.mass
                .replace(",", "")
                .replace(" ", "")
                .toDoubleOrNull()

            mass != null &&
                    mass.isFinite() &&
                    mass >= 0 &&
                    mass <= maximum
        }

        return photoMatches && genderMatches && massMatches
    }
}