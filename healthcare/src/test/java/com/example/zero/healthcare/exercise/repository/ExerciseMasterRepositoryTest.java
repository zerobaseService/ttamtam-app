package com.example.zero.healthcare.exercise.repository;

import com.example.zero.healthcare.Entity.exercise.ExerciseMaster;
import com.example.zero.healthcare.repository.ExerciseMasterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExerciseMasterRepositoryTest {

    @Autowired
    private ExerciseMasterRepository repository;

    private ExerciseMaster buildExercise(String id, String name) {
        ExerciseMaster e = new ExerciseMaster();
        e.setId(id);
        e.setName(name);
        e.setLevel("beginner");
        e.setCategory("strength");
        e.setEquipment("barbell");
        return e;
    }

    @Test
    @DisplayName("저장된 운동 종목 전체 조회")
    void findAll_returnsAllPersistedExercises() {
        repository.save(buildExercise("Bench_Press", "Bench Press"));
        repository.save(buildExercise("Deadlift", "Deadlift"));

        List<ExerciseMaster> result = repository.findAll();

        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("존재하는 ID로 조회하면 해당 엔티티를 반환한다")
    void findById_existing_returnsExercise() {
        repository.save(buildExercise("Squat_Test", "Squat"));

        Optional<ExerciseMaster> result = repository.findById("Squat_Test");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Squat");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 empty를 반환한다")
    void findById_unknownId_returnsEmpty() {
        Optional<ExerciseMaster> result = repository.findById("UNKNOWN_EXERCISE_XYZ");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("koreanName과 primaryMuscles를 포함해 저장하면 정상 영속된다")
    void save_persistsKoreanNameAndPrimaryMuscles() {
        ExerciseMaster e = buildExercise("PullUp_Test", "Pull-Up");
        e.setKoreanName("풀업");
        e.setPrimaryMuscles(Set.of("lats", "biceps"));
        repository.save(e);

        ExerciseMaster found = repository.findById("PullUp_Test").orElseThrow();
        assertThat(found.getKoreanName()).isEqualTo("풀업");
        assertThat(found.getPrimaryMuscles()).containsExactlyInAnyOrder("lats", "biceps");
    }
}
