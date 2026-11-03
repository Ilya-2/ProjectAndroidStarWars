package com.example.projectandroidstarwars.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectandroidstarwars.domain.model.UserProfile
import com.example.projectandroidstarwars.domain.repository.ProfileException
import com.example.projectandroidstarwars.domain.repository.ProfileRepository
import java.net.URI
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val draft: UserProfile = UserProfile(),
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val busy: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState by mutableStateOf(
        ProfileUiState(
            draft = UserProfile(
                fullName = savedStateHandle["draft_name"] ?: "",
                avatarUri = savedStateHandle["draft_avatar"] ?: "",
                resumeUrl = savedStateHandle["draft_resume"] ?: ""
            )
        )
    )
        private set

    init {
        load()
    }

    fun load() {
        if (uiState.loading || uiState.busy) return

        uiState = uiState.copy(
            loading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val profile = repository.load()

                uiState = uiState.copy(
                    profile = profile,
                    loaded = true
                )
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                reportError("Не удалось прочитать профиль.")
            } finally {
                uiState = uiState.copy(loading = false)
            }
        }
    }

    fun beginEditing() {
        if (!uiState.loaded || uiState.busy) return

        updateDraft(uiState.profile)
    }

    fun updateFullName(value: String) {
        if (uiState.busy) return

        updateDraft(
            uiState.draft.copy(fullName = value)
        )
    }

    fun updateResumeUrl(value: String) {
        if (uiState.busy) return

        updateDraft(
            uiState.draft.copy(resumeUrl = value)
        )
    }

    fun importAvatar(uri: String) {
        runOperation(
            defaultMessage = "Не удалось сохранить фотографию."
        ) {
            val localUri = repository.importAvatar(uri)

            updateDraft(
                uiState.draft.copy(avatarUri = localUri)
            )
        }
    }

    fun prepareCamera(
        onReady: (String) -> Unit
    ) {
        runOperation(
            defaultMessage = "Не удалось подготовить камеру."
        ) {
            val uri = repository.createCameraUri()

            // URI переживёт пересоздание Activity, пока открыта камера.
            savedStateHandle["pending_camera_uri"] = uri

            onReady(uri)
        }
    }

    fun cameraFinished(success: Boolean) {
        val uri = savedStateHandle
            .get<String>("pending_camera_uri")

        savedStateHandle.remove<String>("pending_camera_uri")

        if (!success) return

        if (uri == null) {
            reportError("Не удалось найти фотографию с камеры.")
            return
        }

        importAvatar(uri)
    }

    fun save(
        onSaved: () -> Unit
    ) {
        if (!uiState.loaded || uiState.busy) return

        val profile = uiState.draft.copy(
            fullName = uiState.draft.fullName.trim(),
            resumeUrl = uiState.draft.resumeUrl.trim()
        )

        if (profile.resumeUrl.isNotEmpty()) {
            val uri = try {
                URI(profile.resumeUrl)
            } catch (_: Exception) {
                null
            }

            if (
                uri == null ||
                !uri.scheme.equals("https", ignoreCase = true) ||
                uri.host.isNullOrBlank()
            ) {
                reportError(
                    "Введи полную HTTPS-ссылку на PDF-файл."
                )
                return
            }
        }

        runOperation(
            defaultMessage = "Не удалось сохранить профиль."
        ) {
            repository.save(profile)

            uiState = uiState.copy(profile = profile)
            updateDraft(profile)

            onSaved()
        }
    }

    fun downloadResume(
        onDownloaded: (String) -> Unit
    ) {
        if (uiState.profile.resumeUrl.isBlank()) {
            reportError("Сначала добавь ссылку на резюме.")
            return
        }

        runOperation(
            defaultMessage = "Не удалось скачать резюме. " +
                    "Проверь подключение и ссылку."
        ) {
            val uri = repository.downloadResume(
                uiState.profile.resumeUrl
            )

            onDownloaded(uri)
        }
    }

    fun reportError(message: String) {
        uiState = uiState.copy(error = message)
    }

    private fun updateDraft(profile: UserProfile) {
        savedStateHandle["draft_name"] = profile.fullName
        savedStateHandle["draft_avatar"] = profile.avatarUri
        savedStateHandle["draft_resume"] = profile.resumeUrl

        uiState = uiState.copy(
            draft = profile,
            error = null
        )
    }

    private fun runOperation(
        defaultMessage: String,
        block: suspend () -> Unit
    ) {
        if (uiState.busy) return

        uiState = uiState.copy(
            busy = true,
            error = null
        )

        viewModelScope.launch {
            try {
                block()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                reportError(
                    if (error is ProfileException) {
                        error.message ?: defaultMessage
                    } else {
                        defaultMessage
                    }
                )
            } finally {
                uiState = uiState.copy(busy = false)
            }
        }
    }
}