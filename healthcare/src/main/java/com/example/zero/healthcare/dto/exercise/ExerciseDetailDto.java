package com.example.zero.healthcare.dto.exercise;

import com.example.zero.healthcare.Entity.exercise.ExerciseMaster;
import lombok.Getter;

import java.util.Set;

@Getter
public class ExerciseDetailDto {

    private final String id;
    private final String name;
    private final String koreanName;
    private final String force;
    private final String level;
    private final String mechanic;
    private final String equipment;
    private final String category;
    private final Set<String> primaryMuscles;
    private final Set<String> secondaryMuscles;

    public ExerciseDetailDto(ExerciseMaster e) {
        this.id = e.getId();
        this.name = e.getName();
        this.koreanName = e.getKoreanName();
        this.force = e.getForce();
        this.level = e.getLevel();
        this.mechanic = e.getMechanic();
        this.equipment = e.getEquipment();
        this.category = e.getCategory();
        this.primaryMuscles = e.getPrimaryMuscles();
        this.secondaryMuscles = e.getSecondaryMuscles();
    }
}
