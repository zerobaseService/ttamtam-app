package com.example.healthcareapp.data

import java.io.Serializable

data class JournalDetailResponse(
    val journalId: Long,
    val folderId: Long?,
    val workoutDate: String,
    val createdAt: String,
    val startedAt: String?,
    val totalDurationSeconds: Int?,
    val preCondition: PreConditionResponse?,
    val postCondition: PostConditionResponse?,
    val content: String?,
    val painRecords: List<PainRecordDetailResponse>?,
    val attachments: List<AttachmentResponse>?,
    val exercises: List<ExerciseDetailResponse>?
) : Serializable

data class PreConditionResponse(
    val jointMusclePain: Int?,
    val sleepHours: Int?,
    val sleepQuality: Int?,
    val previousFatigue: Int?,
    val overallCondition: Int?
) : Serializable

data class PostConditionResponse(
    val jointMusclePain: Int,
    val intensityFit: Int,
    val goalAchieved: Int,
    val dizziness: Int,
    val mood: Int,
    val recordedAt: String
) : Serializable

data class PainRecordDetailResponse(
    val timing: String,
    val bodyPart: String,
    val side: String,
    val painLevel: Int
) : Serializable

data class AttachmentResponse(
    val id: Long,
    val imageUrl: String,
    val displayOrder: Int
) : Serializable

data class ExerciseDetailResponse(
    val exerciseId: Long,
    val exerciseName: String,
    val displayOrder: Int,
    val sets: List<SetDetailResponse>
) : Serializable

data class SetDetailResponse(
    val setNumber: Int,
    val reps: Int,
    val weightKg: Double,
    val durationMinutes: Int?
) : Serializable
