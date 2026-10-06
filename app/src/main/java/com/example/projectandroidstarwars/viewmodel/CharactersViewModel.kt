package com.example.projectandroidstarwars.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.projectandroidstarwars.data.CharacterRepository
import com.example.projectandroidstarwars.model.StarWarsCharacter

class CharactersViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val repository = CharacterRepository()

    var searchQuery by mutableStateOf(
        savedStateHandle.get<String>("search_query") ?: ""
    )
        private set

    val characters: List<StarWarsCharacter>
        get() {
            val query = searchQuery.trim()

            return repository.getCharacters().filter { character ->
                character.name.contains(
                    other = query,
                    ignoreCase = true
                )
            }
        }

    fun updateSearchQuery(value: String) {
        searchQuery = value
        savedStateHandle["search_query"] = value
    }

    fun getCharacterById(id: Int): StarWarsCharacter? {
        return repository.getCharacterById(id)
    }
}