package com.example.healthcareapp.data

object PainRecordFormatter {
    fun displaySummary(record: PainRecord): String =
        listOf(record.side, record.bodyPartName, "${record.painLevel}단계")
            .filter { it.isNotBlank() }
            .joinToString(" ")

    fun reasonVisible(reason: String?): Boolean = !reason.isNullOrBlank()
}
