package com.example.projectandroidstarwars.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteCharacterEntity::class],
    version = 1,
    exportSchema = true
)
abstract class StarWarsDatabase : RoomDatabase() {

    abstract fun favoritesDao(): FavoritesDao
}