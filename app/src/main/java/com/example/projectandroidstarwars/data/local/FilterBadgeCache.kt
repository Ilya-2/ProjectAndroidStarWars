package com.example.projectandroidstarwars.data.local

import com.example.projectandroidstarwars.domain.model.CharacterFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FilterBadgeCache(
    initialFilters: CharacterFilters
) {

    private val mutableIsVisible = MutableStateFlow(
        initialFilters.isActive
    )

    val isVisible: StateFlow<Boolean> =
        mutableIsVisible.asStateFlow()

    fun update(filters: CharacterFilters) {
        mutableIsVisible.value = filters.isActive
    }
}