package com.example.projectandroidstarwars.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectandroidstarwars.domain.model.StarWarsCharacter
import com.example.projectandroidstarwars.domain.repository.CharacterLoadException
import com.example.projectandroidstarwars.domain.usecase.GetCharacterUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CharacterDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getCharacter: GetCharacterUseCase
) : ViewModel() {

    private val id: Int =
        savedStateHandle.get<Int>("characterId") ?: -1

    private var job: Job? = null

    var state by mutableStateOf<LoadState<StarWarsCharacter>>(
        LoadState.Loading
    )
        private set

    init {
        retry()
    }

    fun retry() {
        job?.cancel()

        if (id <= 0) {
            state = LoadState.Error(
                "Некорректный номер персонажа."
            )
            return
        }

        state = LoadState.Loading

        job = viewModelScope.launch {
            try {
                state = LoadState.Success(
                    getCharacter(id)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: CharacterLoadException) {
                state = LoadState.Error(
                    e.message ?: "Не удалось загрузить досье."
                )
            } catch (e: Exception) {
                state = LoadState.Error(
                    "Не удалось обработать досье. Повторите попытку."
                )
            }
        }
    }
}