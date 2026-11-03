package com.example.projectandroidstarwars.viewmodel

import com.example.projectandroidstarwars.domain.planets.PlanetLoadException
import com.example.projectandroidstarwars.domain.repository.CharacterLoadException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal fun CoroutineScope.launchWithErrorHandling(
    defaultMessage: String,
    onError: (String) -> Unit,
    onFinally: () -> Unit = {},
    block: suspend CoroutineScope.() -> Unit
): Job {
    val handler = CoroutineExceptionHandler { _, throwable ->
        when (throwable) {
            is CancellationException -> Unit

            is Exception -> {
                onError(
                    throwable.toUserMessage(defaultMessage)
                )
            }

            else -> throw throwable
        }
    }

    return launch(handler) {
        try {
            block()
        } finally {
            onFinally()
        }
    }
}

private fun Exception.toUserMessage(
    defaultMessage: String
): String {
    return when (this) {
        is CharacterLoadException,
        is PlanetLoadException -> {
            message
                ?.takeIf { it.isNotBlank() }
                ?: defaultMessage
        }

        else -> defaultMessage
    }
}