package com.example.healthcareapp.data

data class PreConditionDto(
    val jointMusclePain: Int,
    val sleepHours: Int,
    val sleepQuality: Int,
    val previousFatigue: Int,
    val overallCondition: Int
)

data class PostConditionDto(
    val jointMusclePain: Int,
    val intensityFit: Int,
    val goalAchieved: Int,
    val dizziness: Int,
    val mood: Int
)

data class PainRecordDto(
    val bodyPart: String,
    val side: String,
    val painLevel: Int,
    val painReason: String? = null
)

data class ExerciseSetDto(
    val setNumber: Int,
    val reps: Int,
    val weightKg: Double,
    val durationMinutes: Int? = null
)

data class ExerciseDto(
    val exerciseName: String,
    val displayOrder: Int,
    val sets: List<ExerciseSetDto>
)

data class CreateJournalRequest(
    val workoutDate: String,
    val folderId: Long?,
    val preCondition: PreConditionDto,
    val painRecords: List<PainRecordDto>?,
    val startedAt: String
)

data class CompleteJournalRequest(
    val workoutDate: String,
    val startedAt: String,
    val totalDurationSeconds: Int?,
    val postCondition: PostConditionDto,
    val painRecords: List<PainRecordDto>?,
    val exercises: List<ExerciseDto>,
    val content: String?,
    val workoutType: String?,
    val imageUrls: List<String>?
)

data class JournalCreateResponse(
    val journalId: Long,
    val createdAt: String
)

data class JournalSummaryResponse(
    val journalId: Long,
    val workoutDate: String,
    val createdAt: String,
    val postRecorded: Boolean,
    val preOverallCondition: Int?,
    val contentPreview: String?,
    val workoutType: String?
)
