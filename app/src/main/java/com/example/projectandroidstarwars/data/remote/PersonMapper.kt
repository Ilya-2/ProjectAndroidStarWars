package com.example.projectandroidstarwars.data.remote

import com.example.projectandroidstarwars.domain.model.StarWarsCharacter

private fun String?.displayValue(): String {
    return when {
        this.isNullOrBlank() -> "Неизвестно"
        this == "unknown" -> "Неизвестно"
        this == "n/a" -> "Не применимо"
        else -> this
    }
}

fun PersonDto.toDomain(): StarWarsCharacter {
    val id = url
        ?.trimEnd('/')
        ?.substringAfterLast('/')
        ?.toIntOrNull()
        ?: throw IllegalArgumentException("Отсутствует ID персонажа")

    require(id > 0 && !name.isNullOrBlank()) {
        "Некорректные данные персонажа"
    }

    return StarWarsCharacter(
        id = id,
        name = name.orEmpty(),
        height = height.displayValue(),
        mass = mass.displayValue(),
        birthYear = birthYear.displayValue(),
        gender = gender.displayValue(),
        eyeColor = eyeColor.displayValue(),
        hairColor = hairColor.displayValue(),
        skinColor = skinColor.displayValue(),
        filmCount = films?.size ?: 0
    )
}