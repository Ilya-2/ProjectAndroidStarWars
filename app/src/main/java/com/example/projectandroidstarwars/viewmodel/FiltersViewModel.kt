package com.example.projectandroidstarwars.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectandroidstarwars.domain.model.CharacterFilters
import com.example.projectandroidstarwars.domain.model.GenderFilter
import com.example.projectandroidstarwars.domain.repository.FiltersRepository
import com.example.projectandroidstarwars.domain.state.FilterBadgeCache

class FiltersViewModel(
    private val repository: FiltersRepository,
    private val badgeCache: FilterBadgeCache
) : ViewModel() {

    var uiState by mutableStateOf(FiltersUiState())
        private set

    init {
        loadSaved()
    }

    fun loadSaved() {
        if (uiState.saving) return

        val filters = repository.filters.value

        uiState = FiltersUiState(
            name = filters.name,
            onlyWithPhoto = filters.onlyWithPhoto,
            gender = filters.gender,
            maxMassText = filters.maxMass
                ?.toString()
                ?.removeSuffix(".0")
                ?: ""
        )
    }

    fun updateName(value: String) {
        if (uiState.saving) return

        uiState = uiState.copy(
            name = value,
            error = null
        )
    }

    fun updateOnlyWithPhoto(value: Boolean) {
        if (uiState.saving) return

        uiState = uiState.copy(
            onlyWithPhoto = value,
            error = null
        )
    }

    fun updateGender(value: GenderFilter) {
        if (uiState.saving) return

        uiState = uiState.copy(
            gender = value,
            error = null
        )
    }

    fun updateMaxMass(value: String) {
        if (uiState.saving) return

        uiState = uiState.copy(
            maxMassText = value,
            error = null
        )
    }

    fun resetDraft() {
        if (uiState.saving) return

        uiState = FiltersUiState()
    }

    fun save(onSaved: () -> Unit) {
        val draft = uiState

        if (draft.saving) return

        val input = draft.maxMassText.trim().replace(',', '.')
        val parsedMass = input.toDoubleOrNull()

        if (
            input.isNotEmpty() &&
            (
                    parsedMass == null ||
                            !parsedMass.isFinite() ||
                            parsedMass <= 0
                    )
        ) {
            uiState = draft.copy(
                error = "Введите массу больше нуля, например 80 или 80,5."
            )
            return
        }

        val filters = CharacterFilters(
            name = draft.name.trim(),
            onlyWithPhoto = draft.onlyWithPhoto,
            gender = draft.gender,
            maxMass = if (input.isEmpty()) null else parsedMass
        )

        uiState = draft.copy(
            saving = true,
            error = null
        )

        viewModelScope.launchWithErrorHandling(
            defaultMessage = "Не удалось сохранить настройки.",
            onError = { message ->
                uiState = uiState.copy(
                    error = message
                )
            },
            onFinally = {
                uiState = uiState.copy(
                    saving = false
                )
            }
        ) {
            repository.save(filters)
            badgeCache.update(filters)
            onSaved()
        }
    }
}