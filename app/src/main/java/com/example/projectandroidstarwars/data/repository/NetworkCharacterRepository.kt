package com.example.projectandroidstarwars.data.repository

import com.example.projectandroidstarwars.data.remote.NetworkRequestExecutor
import com.example.projectandroidstarwars.data.remote.SwapiApi
import com.example.projectandroidstarwars.data.remote.toDomain
import com.example.projectandroidstarwars.domain.model.CharacterPage
import com.example.projectandroidstarwars.domain.model.StarWarsCharacter
import com.example.projectandroidstarwars.domain.repository.CharacterLoadException
import com.example.projectandroidstarwars.domain.repository.CharacterRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class NetworkCharacterRepository(
    private val api: SwapiApi,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CharacterRepository {

    private val requests = NetworkRequestExecutor(
        createException = ::CharacterLoadException,
        ioDispatcher = ioDispatcher
    )

    override suspend fun getCharacters(
        search: String,
        page: Int
    ): CharacterPage = requests.execute {
        val searchText = search.trim().takeIf { it.isNotEmpty() }

        val firstResponse = api.getPeople(
            search = searchText,
            page = 1
        )

        val firstResults = requireNotNull(firstResponse.results) {
            "Отсутствует список персонажей"
        }

        val characters = firstResults
            .map { it.toDomain() }
            .distinctBy { it.id }
            .take(MAX_CHARACTERS)
            .toMutableList()

        if (
            characters.size < MAX_CHARACTERS &&
            firstResponse.next != null
        ) {
            val secondResponse = api.getPeople(
                search = searchText,
                page = 2
            )

            val secondResults = requireNotNull(secondResponse.results) {
                "Отсутствует список персонажей"
            }

            for (person in secondResults) {
                if (characters.size >= MAX_CHARACTERS) {
                    break
                }

                val character = person.toDomain()

                if (characters.none { it.id == character.id }) {
                    characters.add(character)
                }
            }
        }

        CharacterPage(
            characters = characters.toList(),
            total = characters.size,
            page = 1,
            hasNext = false
        )
    }

    override suspend fun getCharacter(
        id: Int
    ): StarWarsCharacter = requests.execute {
        require(id > 0)
        api.getPerson(id).toDomain()
    }

    companion object {
        private const val MAX_CHARACTERS = 15
    }
}