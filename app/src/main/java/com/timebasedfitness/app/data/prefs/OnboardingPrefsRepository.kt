package com.timebasedfitness.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class OnboardingPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_HAS_ONBOARDED = booleanPreferencesKey("has_onboarded")
    private val KEY_NUDGE_INTERVAL_MIN = intPreferencesKey("nudge_interval_min")
    private val KEY_NUDGE_MAX_PER_WINDOW = intPreferencesKey("nudge_max_per_window")

    val hasOnboarded: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HAS_ONBOARDED] ?: false
    }

    /** Minutes between in-window reminders. Default 10. Allowed in {5, 10, 15, 30}. */
    val nudgeIntervalMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_NUDGE_INTERVAL_MIN] ?: DEFAULT_NUDGE_INTERVAL_MIN
    }

    /** Max number of nudges per window. Default 6. */
    val nudgeMaxPerWindow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_NUDGE_MAX_PER_WINDOW] ?: DEFAULT_NUDGE_MAX_PER_WINDOW
    }

    suspend fun setHasOnboarded(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAS_ONBOARDED] = completed
        }
    }

    suspend fun setNudgeIntervalMinutes(minutes: Int) {
        val sanitized = minutes.coerceIn(1, 120)
        context.dataStore.edit { preferences ->
            preferences[KEY_NUDGE_INTERVAL_MIN] = sanitized
        }
    }

    suspend fun setNudgeMaxPerWindow(max: Int) {
        val sanitized = max.coerceIn(0, 48)
        context.dataStore.edit { preferences ->
            preferences[KEY_NUDGE_MAX_PER_WINDOW] = sanitized
        }
    }

    companion object {
        const val DEFAULT_NUDGE_INTERVAL_MIN = 10
        const val DEFAULT_NUDGE_MAX_PER_WINDOW = 6
        val NUDGE_INTERVAL_OPTIONS = listOf(5, 10, 15, 30)
    }
}
