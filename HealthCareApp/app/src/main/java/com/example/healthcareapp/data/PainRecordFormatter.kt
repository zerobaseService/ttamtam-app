package com.example.healthcareapp.data

object PainRecordFormatter {
    fun displaySummary(record: PainRecord): String {
        val sideKo = when (record.side.uppercase()) {
            "LEFT" -> "좌"
            "RIGHT" -> "우"
            "CENTER" -> ""
            else -> record.side
        }
        return listOf(sideKo, record.bodyPartName, "${record.painLevel}단계")
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }

    fun reasonVisible(reason: String?): Boolean = !reason.isNullOrBlank()
}
