package com.example.zero.healthcare.repository;

import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JournalRepository extends JpaRepository<WorkoutJournal, Long> {
    List<WorkoutJournal> findByAuthorId(Long authorId);

    List<WorkoutJournal> findByAuthorIdOrderByWorkoutDateDescCreatedAtDesc(Long authorId);

    List<WorkoutJournal> findByAuthorIdAndWorkoutDateOrderByCreatedAtDesc(Long authorId, LocalDate workoutDate);

    List<WorkoutJournal> findByAuthorIdAndWorkoutDateBetweenOrderByWorkoutDateDescCreatedAtDesc(
            Long authorId, LocalDate from, LocalDate to);

    // folderId 지정 케이스
    List<WorkoutJournal> findByAuthorIdAndFolderIdOrderByWorkoutDateDescCreatedAtDesc(Long authorId, Long folderId);

    List<WorkoutJournal> findByAuthorIdAndFolderIdAndWorkoutDateOrderByCreatedAtDesc(
            Long authorId, Long folderId, LocalDate workoutDate);

    List<WorkoutJournal> findByAuthorIdAndFolderIdAndWorkoutDateBetweenOrderByWorkoutDateDescCreatedAtDesc(
            Long authorId, Long folderId, LocalDate from, LocalDate to);

    // unfiled (folderId IS NULL) 케이스
    List<WorkoutJournal> findByAuthorIdAndFolderIdIsNullOrderByWorkoutDateDescCreatedAtDesc(Long authorId);

    List<WorkoutJournal> findByAuthorIdAndFolderIdIsNullAndWorkoutDateOrderByCreatedAtDesc(
            Long authorId, LocalDate workoutDate);

    List<WorkoutJournal> findByAuthorIdAndFolderIdIsNullAndWorkoutDateBetweenOrderByWorkoutDateDescCreatedAtDesc(
            Long authorId, LocalDate from, LocalDate to);

    @Query("SELECT j FROM WorkoutJournal j LEFT JOIN j.postCondition pc " +
           "WHERE j.authorId = :authorId AND j.workoutDate = :date AND pc IS NULL " +
           "ORDER BY j.createdAt DESC")
    List<WorkoutJournal> findPreOnlyJournals(
            @Param("authorId") Long authorId, @Param("date") LocalDate date);

    default Optional<WorkoutJournal> findFirstPreOnlyJournal(Long authorId, LocalDate date) {
        return findPreOnlyJournals(authorId, date).stream().findFirst();
    }

    @Modifying(clearAutomatically = true)
    @Query("UPDATE WorkoutJournal j SET j.deletedAt = CURRENT_TIMESTAMP WHERE j.id = :id")
    void softDeleteById(@Param("id") Long id);
}
