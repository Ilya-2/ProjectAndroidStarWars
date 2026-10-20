package com.example.projectandroidstarwars.data.repository

import com.example.projectandroidstarwars.data.remote.SwapiApi
import com.example.projectandroidstarwars.data.remote.toDomain
import com.example.projectandroidstarwars.domain.model.CharacterPage
import com.example.projectandroidstarwars.domain.model.StarWarsCharacter
import com.example.projectandroidstarwars.domain.repository.CharacterLoadException
import com.example.projectandroidstarwars.domain.repository.CharacterRepository
import com.google.gson.JsonParseException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class NetworkCharacterRepository(
    private val api: SwapiApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CharacterRepository {

    override suspend fun getCharacters(
        search: String,
        page: Int
    ): CharacterPage = request {
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
    ): StarWarsCharacter = request {
        require(id > 0)
        api.getPerson(id).toDomain()
    }

    private suspend fun <T> request(
        block: suspend () -> T
    ): T = withContext(ioDispatcher) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            val message = when (e.code()) {
                404 -> "Данные не найдены."
                429 -> "Слишком много запросов. Попробуйте позже."
                else -> "Ошибка сервера: ${e.code()}. Попробуйте позже."
            }

            throw CharacterLoadException(message, e)
        } catch (e: SocketTimeoutException) {
            throw CharacterLoadException(
                "Сервер долго не отвечает. Повторите попытку.",
                e
            )
        } catch (e: JsonParseException) {
            throw CharacterLoadException(
                "Получен некорректный ответ сервера.",
                e
            )
        } catch (e: IOException) {
            throw CharacterLoadException(
                "Не удалось подключиться. Проверьте интернет.",
                e
            )
        } catch (e: IllegalArgumentException) {
            throw CharacterLoadException(
                "В ответе сервера отсутствуют необходимые данные.",
                e
            )
        }
    }

    companion object {
        private const val MAX_CHARACTERS = 15
    }
}