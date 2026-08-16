package com.timebasedfitness.app.data.content

import kotlinx.serialization.Serializable

@Serializable
data class RoutineContent(
    val title: String,
    val steps: List<String>,
    val stepsByDay: Map<String, List<String>> = emptyMap()
)
