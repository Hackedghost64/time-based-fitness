package com.timebasedfitness.app.data.content

import com.timebasedfitness.app.data.model.RoutineStep
import kotlinx.serialization.Serializable

@Serializable
data class RoutineContent(
    val title: String,
    val steps: List<RoutineStep> = emptyList(),
    val stepsByDay: Map<String, List<RoutineStep>> = emptyMap(),
    val goal: String? = null
)
