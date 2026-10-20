package com.example.projectandroidstarwars.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectandroidstarwars.domain.model.CharacterPage
import com.example.projectandroidstarwars.domain.repository.CharacterLoadException
import com.example.projectandroidstarwars.domain.usecase.GetCharactersUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CharactersViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getCharacters: GetCharactersUseCase
) : ViewModel() {

    var searchQuery by mutableStateOf(
        savedStateHandle.get<String>("draft_query") ?: ""
    )
        private set

    var state by mutableStateOf<LoadState<CharacterPage>>(
        LoadState.Loading
    )
        private set

    private var activeQuery =
        savedStateHandle.get<String>("active_query") ?: ""

    private var requestedPage =
        savedStateHandle.get<Int>("page") ?: 1

    private var job: Job? = null

    init {
        load(requestedPage)
    }

    fun updateSearchQuery(value: String) {
        searchQuery = value
        savedStateHandle["draft_query"] = value
    }

    fun search() {
        activeQuery = searchQuery.trim()
        savedStateHandle["active_query"] = activeQuery
        load(1)
    }

    fun retry() {
        load(requestedPage)
    }

    fun nextPage() {
        val currentState = state

        if (currentState is LoadState.Success) {
            val page = currentState.data

            if (page.hasNext) {
                load(page.page + 1)
            }
        }
    }

    fun previousPage() {
        val currentState = state

        if (currentState is LoadState.Success) {
            val page = currentState.data

            if (page.page > 1) {
                load(page.page - 1)
            }
        }
    }

    private fun load(page: Int) {
        job?.cancel()

        requestedPage = page
        savedStateHandle["page"] = page
        state = LoadState.Loading

        val query = activeQuery

        job = viewModelScope.launch {
            try {
                state = LoadState.Success(
                    getCharacters(query, page)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: CharacterLoadException) {
                state = LoadState.Error(
                    e.message ?: "Не удалось загрузить персонажей."
                )
            } catch (e: Exception) {
                state = LoadState.Error(
                    "Не удалось обработать данные. Повторите попытку."
                )
            }
        }
    }
}