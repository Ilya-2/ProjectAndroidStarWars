package com.example.projectandroidstarwars.data.repository

import com.example.projectandroidstarwars.data.local.FavoritesDao
import com.example.projectandroidstarwars.data.local.toCharacter
import com.example.projectandroidstarwars.data.local.toFavoriteEntity
import com.example.projectandroidstarwars.domain.model.StarWarsCharacter
import com.example.projectandroidstarwars.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomFavoritesRepository(
    private val dao: FavoritesDao
) : FavoritesRepository {

    override fun observeFavorites(): Flow<List<StarWarsCharacter>> {
        return dao.observeAll().map { entities ->
            entities.map { it.toCharacter() }
        }
    }

    override suspend fun save(character: StarWarsCharacter) {
        dao.save(character.toFavoriteEntity())
    }

    override suspend fun remove(id: Int) {
        dao.delete(id)
    }
}