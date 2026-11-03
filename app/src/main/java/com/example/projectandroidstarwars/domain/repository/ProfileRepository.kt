package com.example.projectandroidstarwars.domain.repository

import com.example.projectandroidstarwars.domain.model.UserProfile

interface ProfileRepository {

    suspend fun load(): UserProfile

    suspend fun save(profile: UserProfile)

    suspend fun importAvatar(sourceUri: String): String

    suspend fun createCameraUri(): String

    suspend fun downloadResume(url: String): String
}

class ProfileException(
    message: String
) : Exception(message)