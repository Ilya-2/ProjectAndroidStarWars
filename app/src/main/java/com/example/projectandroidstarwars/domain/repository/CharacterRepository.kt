package com.example.projectandroidstarwars.domain.repository

import com.example.projectandroidstarwars.domain.model.CharacterPage
import com.example.projectandroidstarwars.domain.model.StarWarsCharacter

interface CharacterRepository {

    suspend fun getCharacters(
        search: String,
        page: Int
    ): CharacterPage

    suspend fun getCharacter(
        id: Int
    ): StarWarsCharacter
}

class CharacterLoadException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)