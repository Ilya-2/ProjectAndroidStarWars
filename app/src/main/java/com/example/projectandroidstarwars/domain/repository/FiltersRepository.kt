package com.example.projectandroidstarwars.domain.repository

import com.example.projectandroidstarwars.domain.model.CharacterFilters
import kotlinx.coroutines.flow.StateFlow

interface FiltersRepository {

    val filters: StateFlow<CharacterFilters>

    suspend fun save(filters: CharacterFilters)
}