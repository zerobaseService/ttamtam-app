package com.example.healthcareapp.data

data class ConditionRecord(
    val title: String,
    val questions: List<StatusQuestion1>,
    var memo: String = "",
    var isExpanded: Boolean = false,
    val recordedAt: String? = null,
    val painRecords: List<PainRecord> = emptyList()
)