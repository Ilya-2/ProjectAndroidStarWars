package com.example.projectandroidstarwars.viewmodel

import com.example.projectandroidstarwars.domain.model.GenderFilter

data class FiltersUiState(
    val name: String = "",
    val onlyWithPhoto: Boolean = false,
    val gender: GenderFilter = GenderFilter.ANY,
    val maxMassText: String = "",
    val saving: Boolean = false,
    val error: String? = null
) {
    val saveButtonText: String
        get() = if (saving) {
            "Сохранение…"
        } else {
            "Готово"
        }
}