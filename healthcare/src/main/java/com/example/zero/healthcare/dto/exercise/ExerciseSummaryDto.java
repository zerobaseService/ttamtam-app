package com.example.zero.healthcare.dto.exercise;

import com.example.zero.healthcare.Entity.exercise.ExerciseMaster;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Set;

@Getter
public class ExerciseSummaryDto {

    private final String id;
    private final String name;
    private final String koreanName;
    private final String category;
    private final String equipment;
    private final Set<String> primaryMuscles;
    @JsonProperty("isFavorite")
    private final boolean isFavorite;

    public ExerciseSummaryDto(ExerciseMaster e, boolean isFavorite) {
        this.id = e.getId();
        this.name = e.getName();
        this.koreanName = e.getKoreanName();
        this.category = e.getCategory();
        this.equipment = e.getEquipment();
        this.primaryMuscles = e.getPrimaryMuscles();
        this.isFavorite = isFavorite;
    }
}
