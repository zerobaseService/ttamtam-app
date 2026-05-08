package com.example.zero.healthcare.journal.Entity;

import com.example.zero.healthcare.Entity.journal.WorkoutExercise;
import com.example.zero.healthcare.Entity.journal.WorkoutSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutExerciseTest {

    @Test
    @DisplayName("addSet은 sets 리스트에 추가하고 양방향 연관을 설정한다")
    void addSet_addsToListAndSetsExercise() {
        WorkoutExercise exercise = WorkoutExercise.builder()
                .exerciseName("스쿼트")
                .displayOrder(1)
                .build();
        WorkoutSet set = WorkoutSet.builder()
                .setNumber(1)
                .reps(10)
                .weightKg(new BigDecimal("80.0"))
                .build();

        exercise.addSet(set);

        assertThat(exercise.getSets()).hasSize(1);
        assertThat(exercise.getSets().get(0)).isSameAs(set);
        assertThat(set.getExercise()).isSameAs(exercise);
    }

    @Test
    @DisplayName("addSet을 여러 번 호출하면 모두 리스트에 추가된다")
    void addSet_multiple_addsAll() {
        WorkoutExercise exercise = WorkoutExercise.builder()
                .exerciseName("벤치프레스")
                .displayOrder(1)
                .build();

        exercise.addSet(WorkoutSet.builder().setNumber(1).reps(10).weightKg(new BigDecimal("60.0")).build());
        exercise.addSet(WorkoutSet.builder().setNumber(2).reps(8).weightKg(new BigDecimal("65.0")).build());

        assertThat(exercise.getSets()).hasSize(2);
        assertThat(exercise.getSets()).allMatch(s -> s.getExercise() == exercise);
    }
}
