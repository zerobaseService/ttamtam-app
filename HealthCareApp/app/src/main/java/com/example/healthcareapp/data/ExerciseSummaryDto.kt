package com.example.healthcareapp.data

data class ExerciseSummaryDto(
    val id: String,
    val name: String,
    val koreanName: String,
    val category: String,
    val equipment: String,
    val primaryMuscles: Set<String>,
    val isFavorite: Boolean
)

fun ExerciseSummaryDto.toExerciseItem(): ExerciseItem = ExerciseItem(
    id = this.id,
    name = this.koreanName.ifBlank { this.name },
    bodyPart = this.category,
    gifUrl = "",
    isSelected = false,
    target = this.primaryMuscles.joinToString(","),
    isFavorite = this.isFavorite
)
