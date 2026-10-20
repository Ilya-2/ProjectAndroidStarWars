package com.example.projectandroidstarwars.domain.repository

import com.example.projectandroidstarwars.domain.model.StarWarsCharacter
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {

    fun observeFavorites(): Flow<List<StarWarsCharacter>>

    suspend fun save(character: StarWarsCharacter)

    suspend fun remove(id: Int)
}