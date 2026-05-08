package com.example.zero.healthcare.dto.journal;

import com.example.zero.healthcare.Entity.journal.WorkoutSet;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class SetResponseDto {

    private final Integer setNumber;
    private final Integer reps;
    private final BigDecimal weightKg;

    public SetResponseDto(WorkoutSet set) {
        this.setNumber = set.getSetNumber();
        this.reps = set.getReps();
        this.weightKg = set.getWeightKg();
    }
}
