package com.example.projectandroidstarwars.viewmodel

import com.example.projectandroidstarwars.domain.model.StarWarsCharacter

data class CharacterUiModel(
    val id: Int,
    val name: String,
    val heightText: String,
    val dossierTitle: String,
    val heightValue: String,
    val massValue: String,
    val birthYear: String,
    val gender: String,
    val eyeColor: String,
    val hairColor: String,
    val skinColor: String,
    val filmCountText: String
)

data class CharacterListUiModel(
    val characters: List<CharacterUiModel>,
    val countText: String
)

data class FavoriteUiState(
    val favoriteIds: Set<Int> = emptySet(),
    val pendingIds: Set<Int> = emptySet(),
    val ready: Boolean = false,
    val errorMessage: String? = null
)

internal fun StarWarsCharacter.toUiModel(): CharacterUiModel {
    val formattedHeight = formatMeasurement(height, "см")
    val formattedMass = formatMeasurement(mass, "кг")

    return CharacterUiModel(
        id = id,
        name = name,
        heightText = "Рост: $formattedHeight",
        dossierTitle = "Досье №$id",
        heightValue = formattedHeight,
        massValue = formattedMass,
        birthYear = birthYear,
        gender = gender,
        eyeColor = eyeColor,
        hairColor = hairColor,
        skinColor = skinColor,
        filmCountText = filmCount.toString()
    )
}

private fun formatMeasurement(
    value: String,
    unit: String
): String {
    val number = value
        .replace(",", "")
        .replace(" ", "")
        .toDoubleOrNull()

    return if (number != null && number.isFinite()) {
        "$value $unit"
    } else {
        value
    }
}