package com.example.projectandroidstarwars.data.remote

import com.google.gson.JsonParseException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class NetworkRequestExecutor(
    private val createException: (String, Throwable) -> Exception,
    private val notFoundMessage: String = "Данные не найдены.",
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun <T> execute(
        block: suspend () -> T
    ): T = withContext(ioDispatcher) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val message = when (e) {
                is HttpException -> {
                    when (e.code()) {
                        404 -> notFoundMessage

                        429 -> {
                            "Слишком много запросов. Попробуйте позже."
                        }

                        else -> {
                            "Ошибка сервера: ${e.code()}. Попробуйте позже."
                        }
                    }
                }

                is SocketTimeoutException -> {
                    "Сервер долго не отвечает. Повторите попытку."
                }

                is JsonParseException -> {
                    "Получен некорректный ответ сервера."
                }

                is IOException -> {
                    "Не удалось подключиться. Проверьте интернет."
                }

                is IllegalArgumentException -> {
                    "В ответе сервера отсутствуют необходимые данные."
                }

                else -> throw e
            }

            throw createException(message, e)
        }
    }
}