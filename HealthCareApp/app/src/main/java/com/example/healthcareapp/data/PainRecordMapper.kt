package com.example.healthcareapp.data

import com.example.healthcareapp.util.BodyPartMapper

object PainRecordMapper {
    fun fromDetail(items: List<PainRecordDetailResponse>?, timing: PainTiming): List<PainRecord> =
        items.orEmpty()
            .filter { it.timing.equals(timing.name, ignoreCase = true) }
            .map { PainRecord(BodyPartMapper.toDisplayBodyPart(it.bodyPart), it.side, it.painLevel, it.painReason) }

    fun toServer(state: PainSelectionState): List<PainRecordDto> =
        state.all.map { sp ->
            PainRecordDto(
                bodyPart = BodyPartMapper.toServerBodyPart(sp.record.bodyPartName),
                side = BodyPartMapper.toServerBodySide(sp.record.side),
                painLevel = sp.record.painLevel,
                painReason = sp.record.painReason
            )
        }
}
