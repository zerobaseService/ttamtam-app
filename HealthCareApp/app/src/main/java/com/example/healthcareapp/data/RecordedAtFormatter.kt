package com.example.healthcareapp.data

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object RecordedAtFormatter {
    private val OUTPUT_FORMAT = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREA)

    fun format(isoString: String?): String? {
        if (isoString == null) return null
        return try {
            LocalDateTime.parse(isoString).format(OUTPUT_FORMAT)
        } catch (e: DateTimeParseException) {
            null
        }
    }
}
