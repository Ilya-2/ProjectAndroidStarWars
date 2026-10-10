package com.example.projectandroidstarwars.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SwapiApi {

    @GET("people/")
    suspend fun getPeople(
        @Query("search") search: String?,
        @Query("page") page: Int
    ): PeopleResponseDto

    @GET("people/{id}/")
    suspend fun getPerson(
        @Path("id") id: Int
    ): PersonDto
}