package com.example.zero.healthcare.dto.journal;

import com.example.zero.healthcare.Entity.journal.JournalPostCondition;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostConditionResponseDto {

    private final Integer jointMusclePain;
    private final Integer intensityFit;
    private final Integer goalAchieved;
    private final Integer dizziness;
    private final Integer mood;
    private final LocalDateTime recordedAt;

    public PostConditionResponseDto(JournalPostCondition post) {
        this.jointMusclePain = post.getJointMusclePain();
        this.intensityFit = post.getIntensityFit();
        this.goalAchieved = post.getGoalAchieved();
        this.dizziness = post.getDizziness();
        this.mood = post.getMood();
        this.recordedAt = post.getRecordedAt();
    }
}
