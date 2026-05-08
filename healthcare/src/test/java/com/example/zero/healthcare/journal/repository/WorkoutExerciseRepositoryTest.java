package com.example.zero.healthcare.journal.repository;

import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.Entity.journal.WorkoutExercise;
import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import com.example.zero.healthcare.Entity.journal.WorkoutSet;
import com.example.zero.healthcare.repository.JournalRepository;
import com.example.zero.healthcare.repository.UserRepository;
import com.example.zero.healthcare.repository.WorkoutExerciseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WorkoutExerciseRepositoryTest {

    @Autowired private WorkoutExerciseRepository exerciseRepository;
    @Autowired private JournalRepository journalRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TestEntityManager em;

    private User saveUser() {
        return userRepository.save(new User("ex-" + UUID.randomUUID() + "@test.com", "token", "tester"));
    }

    private WorkoutJournal buildJournal(Long authorId) {
        return WorkoutJournal.builder()
                .authorId(authorId)
                .workoutDate(LocalDate.of(2026, 4, 20))
                .build();
    }

    @Test
    @DisplayName("exercise와 set이 journal cascade로 함께 저장된다")
    void save_cascadeFromJournal_persistsExercisesAndSets() {
        User user = saveUser();
        WorkoutJournal journal = buildJournal(user.getId());

        WorkoutExercise exercise = WorkoutExercise.builder().exerciseName("스쿼트").displayOrder(1).build();
        exercise.addSet(WorkoutSet.builder().setNumber(1).reps(10).weightKg(new BigDecimal("80.0")).build());
        journal.addExercise(exercise);

        journalRepository.save(journal);
        em.flush();
        em.clear();

        List<WorkoutExercise> exercises = exerciseRepository.findByJournalId(journal.getId());
        assertThat(exercises).hasSize(1);
        assertThat(exercises.get(0).getExerciseName()).isEqualTo("스쿼트");
        assertThat(exercises.get(0).getSets()).hasSize(1);
        assertThat(exercises.get(0).getSets().get(0).getReps()).isEqualTo(10);
    }

    @Test
    @DisplayName("exercises는 displayOrder 오름차순으로 정렬된다")
    void findByJournalId_sortedByDisplayOrderAsc() {
        User user = saveUser();
        WorkoutJournal journal = buildJournal(user.getId());

        journal.addExercise(WorkoutExercise.builder().exerciseName("데드리프트").displayOrder(2).build());
        journal.addExercise(WorkoutExercise.builder().exerciseName("스쿼트").displayOrder(1).build());

        journalRepository.save(journal);
        em.flush();
        em.clear();

        List<WorkoutExercise> exercises = exerciseRepository.findByJournalId(journal.getId());
        assertThat(exercises).hasSize(2);
        assertThat(exercises.get(0).getExerciseName()).isEqualTo("스쿼트");
        assertThat(exercises.get(1).getExerciseName()).isEqualTo("데드리프트");
    }
}
