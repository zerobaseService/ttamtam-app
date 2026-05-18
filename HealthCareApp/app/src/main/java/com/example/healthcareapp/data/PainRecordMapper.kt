package com.example.healthcareapp.data

object PainRecordMapper {
    fun fromDetail(items: List<PainRecordDetailResponse>?, timing: PainTiming): List<PainRecord> =
        items.orEmpty()
            .filter { it.timing.equals(timing.name, ignoreCase = true) }
            .map { PainRecord(it.bodyPart, it.side, it.painLevel, it.painReason) }
}
