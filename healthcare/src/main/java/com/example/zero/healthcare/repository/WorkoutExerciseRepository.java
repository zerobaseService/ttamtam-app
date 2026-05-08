package com.example.zero.healthcare.repository;

import com.example.zero.healthcare.Entity.journal.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {
    List<WorkoutExercise> findByJournalIdOrderByDisplayOrderAsc(Long journalId);

    default List<WorkoutExercise> findByJournalId(Long journalId) {
        return findByJournalIdOrderByDisplayOrderAsc(journalId);
    }
}
