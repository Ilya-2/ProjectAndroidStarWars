package com.example.projectandroidstarwars.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.projectandroidstarwars.domain.model.StarWarsCharacter

@Entity(tableName = "favorite_characters")
data class FavoriteCharacterEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val height: String,
    val mass: String,
    val birthYear: String,
    val gender: String,
    val eyeColor: String,
    val hairColor: String,
    val skinColor: String,
    val filmCount: Int
)

fun FavoriteCharacterEntity.toCharacter(): StarWarsCharacter {
    return StarWarsCharacter(
        id = id,
        name = name,
        height = height,
        mass = mass,
        birthYear = birthYear,
        gender = gender,
        eyeColor = eyeColor,
        hairColor = hairColor,
        skinColor = skinColor,
        filmCount = filmCount
    )
}

fun StarWarsCharacter.toFavoriteEntity(): FavoriteCharacterEntity {
    return FavoriteCharacterEntity(
        id = id,
        name = name,
        height = height,
        mass = mass,
        birthYear = birthYear,
        gender = gender,
        eyeColor = eyeColor,
        hairColor = hairColor,
        skinColor = skinColor,
        filmCount = filmCount
    )
}