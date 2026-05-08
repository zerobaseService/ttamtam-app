package com.example.zero.healthcare.journal.dto;

import com.example.zero.healthcare.dto.journal.CompleteJournalRequest;
import com.example.zero.healthcare.dto.journal.ExerciseDto;
import com.example.zero.healthcare.dto.journal.PostConditionDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CompleteJournalRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private PostConditionDto validPostCondition() {
        PostConditionDto dto = new PostConditionDto();
        dto.setJointMusclePain(6);
        dto.setIntensityFit(7);
        dto.setGoalAchieved(8);
        dto.setDizziness(2);
        dto.setMood(9);
        return dto;
    }

    private CompleteJournalRequest validRequest() {
        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(LocalDate.of(2026, 4, 20));
        req.setStartedAt(LocalDateTime.of(2026, 4, 20, 9, 0));
        req.setPostCondition(validPostCondition());
        return req;
    }

    @Test
    @DisplayName("workoutDate가 null이면 violations가 발생한다")
    void validate_workoutDate_required() {
        CompleteJournalRequest req = validRequest();
        req.setWorkoutDate(null);

        Set<ConstraintViolation<CompleteJournalRequest>> violations = validator.validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("workoutDate"));
    }

    @Test
    @DisplayName("postCondition이 null이면 violations가 발생한다")
    void validate_postCondition_required() {
        CompleteJournalRequest req = validRequest();
        req.setPostCondition(null);

        Set<ConstraintViolation<CompleteJournalRequest>> violations = validator.validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("postCondition"));
    }

    @Test
    @DisplayName("exercises에 빈 exerciseName이 있으면 violations가 발생한다")
    void validate_exercises_blankExerciseName_hasViolations() {
        ExerciseDto exercise = new ExerciseDto();
        exercise.setExerciseName("");
        exercise.setDisplayOrder(1);

        CompleteJournalRequest req = validRequest();
        req.setExercises(List.of(exercise));

        Set<ConstraintViolation<CompleteJournalRequest>> violations = validator.validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("exerciseName"));
    }

    @Test
    @DisplayName("imageUrls가 5개를 초과하면 violations가 발생한다")
    void validate_imageUrls_over5_hasViolations() {
        CompleteJournalRequest req = validRequest();
        req.setImageUrls(List.of(
                "https://a.com/1.jpg", "https://a.com/2.jpg", "https://a.com/3.jpg",
                "https://a.com/4.jpg", "https://a.com/5.jpg", "https://a.com/6.jpg"
        ));

        Set<ConstraintViolation<CompleteJournalRequest>> violations = validator.validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("imageUrls"));
    }

    @Test
    @DisplayName("imageUrls 원소가 1024자를 초과하면 violations가 발생한다")
    void validate_imageUrls_urlOver1024_hasViolations() {
        String longUrl = "https://a.com/" + "x".repeat(1024);
        CompleteJournalRequest req = validRequest();
        req.setImageUrls(List.of(longUrl));

        Set<ConstraintViolation<CompleteJournalRequest>> violations = validator.validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("imageUrls"));
    }

    @Test
    @DisplayName("content가 5000자를 초과하면 violations가 발생한다")
    void validate_content_over5000_hasViolations() {
        CompleteJournalRequest req = validRequest();
        req.setContent("a".repeat(5001));

        Set<ConstraintViolation<CompleteJournalRequest>> violations = validator.validate(req);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("content"));
    }

    @Test
    @DisplayName("folderId가 null이어도 violations가 없다")
    void validate_folderIdNullable_noViolations() {
        CompleteJournalRequest req = validRequest();
        req.setFolderId(null);

        Set<ConstraintViolation<CompleteJournalRequest>> violations = validator.validate(req);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("exercises가 null이어도 violations가 없다")
    void validate_exercisesNullable_noViolations() {
        CompleteJournalRequest req = validRequest();
        req.setExercises(null);

        Set<ConstraintViolation<CompleteJournalRequest>> violations = validator.validate(req);

        assertThat(violations).isEmpty();
    }
}
