package com.example.zero.healthcare.dto.journal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PreConditionDto {

    @Min(1) @Max(10)
    private Integer jointMusclePain;

    @Min(1) @Max(10)
    private Integer sleepHours;

    @Min(1) @Max(10)
    private Integer sleepQuality;

    @Min(1) @Max(10)
    private Integer previousFatigue;

    @Min(1) @Max(10)
    private Integer overallCondition;
}
