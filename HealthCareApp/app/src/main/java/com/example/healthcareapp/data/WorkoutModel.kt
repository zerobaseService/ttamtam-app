package com.example.healthcareapp.data

import java.io.Serializable

data class ExerciseRecord(
    val id: Int,
    val name: String,
    val sets: MutableList<ExerciseSet>
) : Serializable

data class ExerciseSet(
    var setNumber: Int,
    var weight: Int,
    var reps: Int
) : Serializable