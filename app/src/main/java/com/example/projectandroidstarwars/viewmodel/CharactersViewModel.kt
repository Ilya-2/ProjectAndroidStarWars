package com.example.projectandroidstarwars.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectandroidstarwars.domain.model.CharacterFilters
import com.example.projectandroidstarwars.domain.model.GenderFilter
import com.example.projectandroidstarwars.domain.model.StarWarsCharacter
import com.example.projectandroidstarwars.domain.repository.FavoritesRepository
import com.example.projectandroidstarwars.domain.repository.FiltersRepository
import com.example.projectandroidstarwars.domain.state.FilterBadgeCache
import com.example.projectandroidstarwars.domain.usecase.GetCharactersUseCase
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect

class CharactersViewModel(
    private val getCharacters: GetCharactersUseCase,
    private val filtersRepository: FiltersRepository,
    private val badgeCache: FilterBadgeCache,
    private val hasPhoto: (Int) -> Boolean,
    favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val favorites = FavoritesController(
        repository = favoritesRepository,
        scope = viewModelScope
    )

    var searchQuery by mutableStateOf(
        filtersRepository.filters.value.name
    )
        private set

    var state by mutableStateOf<LoadState<CharacterListUiModel>>(
        LoadState.Loading
    )
        private set

    var savingSearch by mutableStateOf(false)
        private set

    val badgeVisible = badgeCache.isVisible

    val favoriteUiState: FavoriteUiState
        get() = favorites.uiState

    private var requestJob: Job? = null
    private var charactersById: Map<Int, StarWarsCharacter> = emptyMap()
    private var hasResumed = false

    init {
        viewModelScope.launchWithErrorHandling(
            defaultMessage = "Не удалось прочитать настройки поиска.",
            onError = { message ->
                state = LoadState.Error(message)
            }
        ) {
            filtersRepository.filters.collect { filters ->
                searchQuery = filters.name
                badgeCache.update(filters)
                load(filters)
            }
        }
    }

    fun onResume() {
        if (hasResumed) {
            retry()
        } else {
            // Первая загрузка уже запускается при подписке на фильтры.
            hasResumed = true
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

        viewModelScope.launchWithErrorHandling(
            defaultMessage = "Не удалось сохранить поисковый запрос.",
            onError = { message ->
                state = LoadState.Error(message)
            },
            onFinally = {
                savingSearch = false
            }
        ) {
            filtersRepository.save(newFilters)
            searchQuery = newFilters.name
            badgeCache.update(newFilters)

            if (oldFilters == newFilters) {
                load(newFilters)
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

    fun setFavorite(
        id: Int,
        favorite: Boolean
    ) {
        val character = charactersById[id] ?: return
        favorites.setFavorite(character, favorite)
    }

    fun dismissFavoriteError() {
        favorites.dismissError()
    }

    private fun load(filters: CharacterFilters) {
        requestJob?.cancel()
        charactersById = emptyMap()
        state = LoadState.Loading

        requestJob = viewModelScope.launchWithErrorHandling(
            defaultMessage = "Не удалось обработать список персонажей.",
            onError = { message ->
                state = LoadState.Error(message)
            }
        ) {
            val page = getCharacters(
                search = filters.name,
                page = 1
            )

            val filteredCharacters = page.characters.filter {
                matchesFilters(it, filters)
            }

            charactersById = filteredCharacters.associateBy { it.id }

            state = LoadState.Success(
                CharacterListUiModel(
                    characters = filteredCharacters.map {
                        it.toUiModel()
                    },
                    countText = "Показано: ${filteredCharacters.size}"
                )
            )
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