package com.example.projectandroidstarwars.domain.model

enum class GenderFilter(
    val title: String
) {
    ANY("Любой"),
    MALE("Мужской"),
    FEMALE("Женский"),
    NOT_APPLICABLE("Не применимо")
}

data class CharacterFilters(
    val name: String = "",
    val onlyWithPhoto: Boolean = false,
    val gender: GenderFilter = GenderFilter.ANY,
    val maxMass: Double? = null
) {
    val isActive: Boolean
        get() = name.isNotBlank() ||
                onlyWithPhoto ||
                gender != GenderFilter.ANY ||
                maxMass != null
}