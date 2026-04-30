package com.example.zero.healthcare.repository;

import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface JournalRepository extends JpaRepository<WorkoutJournal, Long> {
    List<WorkoutJournal> findByAuthorId(Long authorId);

    List<WorkoutJournal> findByAuthorIdOrderByWorkoutDateDescCreatedAtDesc(Long authorId);

    List<WorkoutJournal> findByAuthorIdAndWorkoutDateOrderByCreatedAtDesc(Long authorId, LocalDate workoutDate);

    List<WorkoutJournal> findByAuthorIdAndWorkoutDateBetweenOrderByWorkoutDateDescCreatedAtDesc(
            Long authorId, LocalDate from, LocalDate to);

    java.util.Optional<WorkoutJournal> findFirstByAuthorIdAndWorkoutDateAndPostRecordedAtIsNullOrderByCreatedAtDesc(
            Long authorId, LocalDate workoutDate);
}
