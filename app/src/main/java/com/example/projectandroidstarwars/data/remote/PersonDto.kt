package com.example.projectandroidstarwars.data.remote

import com.google.gson.annotations.SerializedName

data class PeopleResponseDto(
    val count: Int? = null,
    val next: String? = null,
    val results: List<PersonDto>? = null
)

data class PersonDto(
    val url: String? = null,
    val name: String? = null,
    val height: String? = null,
    val mass: String? = null,

    @SerializedName("birth_year")
    val birthYear: String? = null,

    val gender: String? = null,

    @SerializedName("eye_color")
    val eyeColor: String? = null,

    @SerializedName("hair_color")
    val hairColor: String? = null,

    @SerializedName("skin_color")
    val skinColor: String? = null,

    val films: List<String>? = null
)