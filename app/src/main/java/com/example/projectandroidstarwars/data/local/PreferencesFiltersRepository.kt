package com.example.projectandroidstarwars.data.local

import android.content.Context
import com.example.projectandroidstarwars.domain.model.CharacterFilters
import com.example.projectandroidstarwars.domain.model.GenderFilter
import com.example.projectandroidstarwars.domain.repository.FiltersRepository
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class PreferencesFiltersRepository(
    context: Context
) : FiltersRepository {

    private val preferences = context.applicationContext
        .getSharedPreferences(
            "character_filters",
            Context.MODE_PRIVATE
        )

    private val mutableFilters = MutableStateFlow(
        readFilters()
    )

    override val filters: StateFlow<CharacterFilters> =
        mutableFilters.asStateFlow()

    private val saveMutex = Mutex()

    private fun readFilters(): CharacterFilters {
        val genderName = preferences.getString(
            KEY_GENDER,
            GenderFilter.ANY.name
        )

        val gender = GenderFilter.entries.firstOrNull {
            it.name == genderName
        } ?: GenderFilter.ANY

        val maxMass = preferences
            .getString(KEY_MAX_MASS, null)
            ?.toDoubleOrNull()
            ?.takeIf { it.isFinite() && it > 0 }

        return CharacterFilters(
            name = preferences.getString(KEY_NAME, "") ?: "",
            onlyWithPhoto = preferences.getBoolean(
                KEY_ONLY_WITH_PHOTO,
                false
            ),
            gender = gender,
            maxMass = maxMass
        )
    }

    override suspend fun save(filters: CharacterFilters) {
        require(
            filters.maxMass == null ||
                    (filters.maxMass.isFinite() && filters.maxMass > 0)
        ) {
            "Масса должна быть положительным числом."
        }

        val normalizedFilters = filters.copy(
            name = filters.name.trim()
        )

        withContext(Dispatchers.IO) {
            saveMutex.withLock {
                val editor = preferences.edit()
                    .putString(
                        KEY_NAME,
                        normalizedFilters.name
                    )
                    .putBoolean(
                        KEY_ONLY_WITH_PHOTO,
                        normalizedFilters.onlyWithPhoto
                    )
                    .putString(
                        KEY_GENDER,
                        normalizedFilters.gender.name
                    )

                if (normalizedFilters.maxMass == null) {
                    editor.remove(KEY_MAX_MASS)
                } else {
                    editor.putString(
                        KEY_MAX_MASS,
                        normalizedFilters.maxMass.toString()
                    )
                }

                if (!editor.commit()) {
                    throw IOException(
                        "Не удалось сохранить настройки."
                    )
                }

                mutableFilters.value = normalizedFilters
            }
        }
    }

    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_ONLY_WITH_PHOTO = "only_with_photo"
        private const val KEY_GENDER = "gender"
        private const val KEY_MAX_MASS = "max_mass"
    }
}