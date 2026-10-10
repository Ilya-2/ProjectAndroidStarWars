package com.example.projectandroidstarwars.domain.usecase

import com.example.projectandroidstarwars.domain.model.CharacterPage
import com.example.projectandroidstarwars.domain.model.StarWarsCharacter
import com.example.projectandroidstarwars.domain.repository.CharacterRepository

class GetCharactersUseCase(
    private val repository: CharacterRepository
) {

    suspend operator fun invoke(
        search: String,
        page: Int
    ): CharacterPage {
        return repository.getCharacters(
            search = search.trim(),
            page = page.coerceAtLeast(1)
        )
    }
}

class GetCharacterUseCase(
    private val repository: CharacterRepository
) {

    suspend operator fun invoke(
        id: Int
    ): StarWarsCharacter {
        return repository.getCharacter(id)
    }
}