package com.timebasedfitness.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    val hasOnboarded: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HAS_ONBOARDED] ?: false
    }

    suspend fun setHasOnboarded(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAS_ONBOARDED] = completed
        }
    }
}
