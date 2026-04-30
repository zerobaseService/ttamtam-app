package com.example.zero.healthcare.journal.dto;

import com.example.zero.healthcare.dto.journal.CreateJournalRequest;
import com.example.zero.healthcare.dto.journal.PainRecordDto;
import com.example.zero.healthcare.dto.journal.PreConditionDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateJournalRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private PreConditionDto validPreCondition() {
        return new PreConditionDto(5, 7, 6, 4, 8);
    }

    private LocalDate validWorkoutDate() {
        return LocalDate.of(2026, 4, 20);
    }

    private PainRecordDto painRecord(String bodyPart, String side, Integer painLevel) {
        return new PainRecordDto(bodyPart, side, painLevel);
    }

    @Test
    @DisplayName("preCondition이 null이면 violations가 발생한다")
    void validate_nullPreCondition_hasViolations() {
        CreateJournalRequest req = new CreateJournalRequest(validWorkoutDate(), null, null, null);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("preCondition"));
    }

    @Test
    @DisplayName("preCondition 필드가 null이면 violations가 발생한다")
    void validate_preConditionFieldNull_hasViolations() {
        PreConditionDto pre = new PreConditionDto(null, 7, 6, 4, 8);
        CreateJournalRequest req = new CreateJournalRequest(validWorkoutDate(), null, pre, null);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().contains("jointMusclePain"));
    }

    @Test
    @DisplayName("preCondition 필드가 1-10 범위 초과이면 violations가 발생한다")
    void validate_preConditionOutOfRange_hasViolations() {
        PreConditionDto pre = new PreConditionDto(11, 7, 6, 4, 8);
        CreateJournalRequest req = new CreateJournalRequest(validWorkoutDate(), null, pre, null);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().contains("jointMusclePain"));
    }

    @Test
    @DisplayName("painRecords가 null이면 통과한다")
    void validate_nullPainRecords_noViolations() {
        CreateJournalRequest req = new CreateJournalRequest(validWorkoutDate(), null, validPreCondition(), null);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("painRecords가 빈 배열이면 통과한다")
    void validate_emptyPainRecords_noViolations() {
        CreateJournalRequest req = new CreateJournalRequest(validWorkoutDate(), null, validPreCondition(), List.of());
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("painRecords 내 bodyPart+side 중복이 있으면 violations가 발생한다")
    void validate_duplicatePainRecord_hasViolations() {
        List<PainRecordDto> records = List.of(
                painRecord("SHOULDER", "LEFT", 5),
                painRecord("SHOULDER", "LEFT", 7)
        );
        CreateJournalRequest req = new CreateJournalRequest(validWorkoutDate(), null, validPreCondition(), records);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().contains("painRecordsUnique"));
    }

    @Test
    @DisplayName("painLevel이 null이면 통과한다 (PainRecordDto 필드 nullable)")
    void validate_painLevelNull_noViolations() {
        List<PainRecordDto> records = List.of(painRecord("SHOULDER", "LEFT", null));
        CreateJournalRequest req = new CreateJournalRequest(validWorkoutDate(), null, validPreCondition(), records);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("painLevel이 범위 초과이면 violations가 발생한다")
    void validate_painLevelOutOfRange_hasViolations() {
        List<PainRecordDto> records = List.of(painRecord("SHOULDER", "LEFT", 11));
        CreateJournalRequest req = new CreateJournalRequest(validWorkoutDate(), null, validPreCondition(), records);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().contains("painLevel"));
    }

    @Test
    @DisplayName("workoutDate가 null이면 violations가 발생한다")
    void workoutDate_null_invalid() {
        CreateJournalRequest req = new CreateJournalRequest(null, null, validPreCondition(), null);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("workoutDate"));
    }

    @Test
    @DisplayName("workoutDate가 미래 날짜이면 violations가 발생한다")
    void workoutDate_future_invalid() {
        LocalDate future = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1);
        CreateJournalRequest req = new CreateJournalRequest(future, null, validPreCondition(), null);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("workoutDate"));
    }

    @Test
    @DisplayName("workoutDate가 오늘이면 violations가 없다")
    void workoutDate_today_valid() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        CreateJournalRequest req = new CreateJournalRequest(today, null, validPreCondition(), null);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("workoutDate"));
    }

    @Test
    @DisplayName("workoutDate가 과거 날짜이면 violations가 없다")
    void workoutDate_past_valid() {
        CreateJournalRequest req = new CreateJournalRequest(LocalDate.of(2026, 4, 1), null, validPreCondition(), null);
        Set<ConstraintViolation<CreateJournalRequest>> violations = validator.validate(req);
        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("workoutDate"));
    }
}
