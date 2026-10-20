package com.example.projectandroidstarwars.viewmodel

sealed interface LoadState<out T> {

    data object Loading : LoadState<Nothing>

    data class Success<T>(
        val data: T
    ) : LoadState<T>

    data class Error(
        val message: String
    ) : LoadState<Nothing>
}