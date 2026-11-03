package com.example.projectandroidstarwars.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectandroidstarwars.data.local.FilterBadgeCache
import com.example.projectandroidstarwars.domain.model.CharacterFilters
import com.example.projectandroidstarwars.domain.model.GenderFilter
import com.example.projectandroidstarwars.domain.repository.FiltersRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class FiltersViewModel(
    private val repository: FiltersRepository,
    private val badgeCache: FilterBadgeCache
) : ViewModel() {

    var name by mutableStateOf("")
        private set

    var onlyWithPhoto by mutableStateOf(false)
        private set

    var gender by mutableStateOf(GenderFilter.ANY)
        private set

    var maxMassText by mutableStateOf("")
        private set

    var saving by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    init {
        loadSaved()
    }

    fun loadSaved() {
        if (saving) return

        val filters = repository.filters.value

        name = filters.name
        onlyWithPhoto = filters.onlyWithPhoto
        gender = filters.gender
        maxMassText = filters.maxMass
            ?.toString()
            ?.removeSuffix(".0")
            ?: ""

        error = null
    }

    fun updateName(value: String) {
        name = value
        error = null
    }

    fun updateOnlyWithPhoto(value: Boolean) {
        onlyWithPhoto = value
        error = null
    }

    fun updateGender(value: GenderFilter) {
        gender = value
        error = null
    }

    fun updateMaxMass(value: String) {
        maxMassText = value
        error = null
    }

    fun resetDraft() {
        name = ""
        onlyWithPhoto = false
        gender = GenderFilter.ANY
        maxMassText = ""
        error = null
    }

    fun save(onSaved: () -> Unit) {
        if (saving) return

        val input = maxMassText.trim().replace(',', '.')
        val parsedMass = input.toDoubleOrNull()

        if (
            input.isNotEmpty() &&
            (
                    parsedMass == null ||
                            !parsedMass.isFinite() ||
                            parsedMass <= 0
                    )
        ) {
            error = "Введите массу больше нуля, например 80 или 80,5."
            return
        }

        val filters = CharacterFilters(
            name = name.trim(),
            onlyWithPhoto = onlyWithPhoto,
            gender = gender,
            maxMass = if (input.isEmpty()) null else parsedMass
        )

        saving = true
        error = null

        viewModelScope.launch {
            try {
                repository.save(filters)
                badgeCache.update(filters)
                onSaved()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = "Не удалось сохранить настройки."
            } finally {
                saving = false
            }
        }
    }
}