package com.timebasedfitness.app.data.content

import android.content.Context
import android.util.Log
import com.timebasedfitness.app.data.model.Category
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val routinesMap: Map<String, RoutineContent> by lazy {
        try {
            val jsonString = context.assets.open("routines.json").bufferedReader().use { it.readText() }
            Json.decodeFromString<Map<String, RoutineContent>>(jsonString)
        } catch (e: Exception) {
            Log.e("ContentRepository", "Failed to load default routines from assets", e)
            emptyMap()
        }
    }

    fun getRoutine(category: Category): RoutineContent? {
        return routinesMap[category.name]
    }
}
