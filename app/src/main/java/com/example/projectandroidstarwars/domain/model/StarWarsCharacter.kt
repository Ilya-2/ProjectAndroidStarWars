package com.example.projectandroidstarwars.domain.model

data class StarWarsCharacter(
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

data class CharacterPage(
    val characters: List<StarWarsCharacter>,
    val total: Int,
    val page: Int,
    val hasNext: Boolean
)