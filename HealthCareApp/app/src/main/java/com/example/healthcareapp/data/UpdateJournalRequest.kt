package com.example.healthcareapp.data

data class UpdateJournalRequest(
    val preCondition: PreConditionDto? = null,
    val postCondition: PostConditionDto? = null,
    val prePainRecords: List<PainRecordDto>? = null,
    val postPainRecords: List<PainRecordDto>? = null,
    val content: String? = null,
    val imageUrls: List<String>? = null
)
