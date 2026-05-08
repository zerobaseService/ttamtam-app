package com.example.zero.healthcare.dto.journal;

import com.example.zero.healthcare.Entity.journal.WorkoutExercise;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ExerciseResponseDto {

    private final Long exerciseId;
    private final String exerciseName;
    private final Integer displayOrder;
    private final List<SetResponseDto> sets;

    public ExerciseResponseDto(WorkoutExercise exercise) {
        this.exerciseId = exercise.getId();
        this.exerciseName = exercise.getExerciseName();
        this.displayOrder = exercise.getDisplayOrder();
        this.sets = exercise.getSets().stream()
                .map(SetResponseDto::new)
                .collect(Collectors.toList());
    }
}
