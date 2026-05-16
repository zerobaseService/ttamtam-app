package com.example.healthcareapp.data

import java.io.Serializable

data class ExerciseRecord(
    val id: Int,
    val name: String,
    val sets: MutableList<ExerciseSet>,
    val isCardio: Boolean = false
) : Serializable

data class ExerciseSet(
    var setNumber: Int,
    var weight: Double,
    var reps: Int,
    var durationMinutes: Int = 0
) : Serializable