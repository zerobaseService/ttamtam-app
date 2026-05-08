package com.example.zero.healthcare.dto.journal;

import com.example.zero.healthcare.Entity.journal.JournalPreCondition;
import lombok.Getter;

@Getter
public class PreConditionResponseDto {

    private final Integer jointMusclePain;
    private final Integer sleepHours;
    private final Integer sleepQuality;
    private final Integer previousFatigue;
    private final Integer overallCondition;

    public PreConditionResponseDto(JournalPreCondition pre) {
        this.jointMusclePain = pre.getJointMusclePain();
        this.sleepHours = pre.getSleepHours();
        this.sleepQuality = pre.getSleepQuality();
        this.previousFatigue = pre.getPreviousFatigue();
        this.overallCondition = pre.getOverallCondition();
    }
}
