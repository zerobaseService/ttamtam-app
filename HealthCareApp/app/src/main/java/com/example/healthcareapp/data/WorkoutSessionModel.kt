package com.example.healthcareapp.data

import java.io.Serializable

data class WorkoutSessionExercise(
    val id: String,
    val name: String,
    val bodyPart: String,
    val sets: MutableList<WorkoutSessionSet>
)

data class WorkoutSessionSet(
    var setNumber: Int,
    var weight: Double = 0.0,
    var reps: Int = 0,
    var durationMinutes: Int = 0
) : Serializable
