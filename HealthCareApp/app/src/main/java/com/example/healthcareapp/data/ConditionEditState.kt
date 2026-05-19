package com.example.healthcareapp.data

import com.example.healthcareapp.util.BodyPartMapper

class ConditionEditState(
    val hasPreCondition: Boolean,
    val hasPostCondition: Boolean,
    var preJointMusclePain: Int = 5,
    var preSleepHours: Int = 6,
    var preSleepQuality: Int = 5,
    var prePreviousFatigue: Int = 5,
    var preOverallCondition: Int = 5,
    var postJointMusclePain: Int = 5,
    var postIntensityFit: Int = 5,
    var postGoalAchieved: Int = 5,
    var postDizziness: Int = 10,
    var postMood: Int = 7,
    val prePainState: PainSelectionState = PainSelectionState(),
    val postPainState: PainSelectionState = PainSelectionState(),
    var memo: String? = null,
    val imageUrls: MutableList<String> = mutableListOf()
) {
    var preConditionDirty = false
    var postConditionDirty = false
    var prePainDirty = false
    var postPainDirty = false
    var memoDirty = false
    var imageDirty = false

    val isAnyDirty: Boolean
        get() = preConditionDirty || postConditionDirty || prePainDirty || postPainDirty || memoDirty || imageDirty

    fun toPreConditionDto(): PreConditionDto? {
        if (!hasPreCondition) return null
        return PreConditionDto(
            jointMusclePain = preJointMusclePain,
            sleepHours = preSleepHours,
            sleepQuality = preSleepQuality,
            previousFatigue = prePreviousFatigue,
            overallCondition = preOverallCondition
        )
    }

    fun toPostConditionDto(): PostConditionDto = PostConditionDto(
        jointMusclePain = postJointMusclePain,
        intensityFit = postIntensityFit,
        goalAchieved = postGoalAchieved,
        dizziness = postDizziness,
        mood = postMood
    )

    companion object {
        fun fromDetail(detail: JournalDetailResponse): ConditionEditState {
            val pre = detail.preCondition
            val post = detail.postCondition
            val state = ConditionEditState(
                hasPreCondition = pre != null,
                hasPostCondition = post != null,
                preJointMusclePain = pre?.jointMusclePain ?: 5,
                preSleepHours = pre?.sleepHours ?: 6,
                preSleepQuality = pre?.sleepQuality ?: 5,
                prePreviousFatigue = pre?.previousFatigue ?: 5,
                preOverallCondition = pre?.overallCondition ?: 5,
                postJointMusclePain = post?.jointMusclePain ?: 5,
                postIntensityFit = post?.intensityFit ?: 5,
                postGoalAchieved = post?.goalAchieved ?: 5,
                postDizziness = post?.dizziness ?: 10,
                postMood = post?.mood ?: 7,
                memo = detail.content,
                imageUrls = detail.attachments
                    ?.sortedBy { it.displayOrder }
                    ?.map { it.imageUrl }
                    ?.toMutableList() ?: mutableListOf()
            )
            detail.painRecords?.forEach { pain ->
                val record = PainRecord(
                    bodyPartName = BodyPartMapper.toDisplayBodyPart(pain.bodyPart),
                    side = pain.side,
                    painLevel = pain.painLevel,
                    painReason = pain.painReason
                )
                if (pain.timing.equals("PRE", ignoreCase = true)) {
                    state.prePainState.addIfAbsent("FRONT", record)
                } else {
                    state.postPainState.addIfAbsent("FRONT", record)
                }
            }
            return state
        }
    }
}
