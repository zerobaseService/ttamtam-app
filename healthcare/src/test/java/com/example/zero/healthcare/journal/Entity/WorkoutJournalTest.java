package com.example.zero.healthcare.journal.Entity;

import com.example.zero.healthcare.Entity.journal.BodyPart;
import com.example.zero.healthcare.Entity.journal.BodySide;
import com.example.zero.healthcare.Entity.journal.JournalAttachment;
import com.example.zero.healthcare.Entity.journal.JournalPainRecord;
import com.example.zero.healthcare.Entity.journal.JournalPostCondition;
import com.example.zero.healthcare.Entity.journal.JournalPreCondition;
import com.example.zero.healthcare.Entity.journal.PainTiming;
import com.example.zero.healthcare.Entity.journal.WorkoutExercise;
import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import com.example.zero.healthcare.dto.journal.PostConditionDto;
import com.example.zero.healthcare.dto.journal.PreConditionDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutJournalTest {

    private WorkoutJournal buildJournal() {
        return WorkoutJournal.builder()
                .authorId(1L)
                .workoutDate(LocalDate.of(2026, 4, 20))
                .build();
    }

    private JournalPainRecord buildRecord(BodyPart part, BodySide side) {
        return JournalPainRecord.builder()
                .timing(PainTiming.PRE)
                .bodyPart(part)
                .side(side)
                .painLevel(5)
                .build();
    }

    private PostConditionDto validPostConditionDto() {
        PostConditionDto dto = new PostConditionDto();
        dto.setJointMusclePain(6);
        dto.setIntensityFit(7);
        dto.setGoalAchieved(8);
        dto.setDizziness(2);
        dto.setMood(9);
        return dto;
    }

    @Test
    @DisplayName("workoutDate를 빌더에 전달하면 해당 날짜로 저장된다")
    void builder_withWorkoutDate_setsWorkoutDate() {
        LocalDate date = LocalDate.of(2026, 4, 20);
        WorkoutJournal journal = WorkoutJournal.builder()
                .authorId(1L)
                .workoutDate(date)
                .build();

        assertThat(journal.getWorkoutDate()).isEqualTo(date);
    }

    @Test
    @DisplayName("addPainRecord는 painRecords 리스트에 추가하고 양방향 연관을 설정한다")
    void addPainRecord_addsToListAndSetsJournal() {
        WorkoutJournal journal = buildJournal();
        JournalPainRecord record = buildRecord(BodyPart.SHOULDER, BodySide.LEFT);

        journal.addPainRecord(record);

        assertThat(journal.getPainRecords()).hasSize(1);
        assertThat(journal.getPainRecords().get(0)).isSameAs(record);
        assertThat(record.getJournal()).isSameAs(journal);
    }

    @Test
    @DisplayName("addPainRecord를 여러 번 호출하면 모두 리스트에 추가된다")
    void addPainRecord_multiple_addsAll() {
        WorkoutJournal journal = buildJournal();

        journal.addPainRecord(buildRecord(BodyPart.SHOULDER, BodySide.LEFT));
        journal.addPainRecord(buildRecord(BodyPart.KNEE, BodySide.RIGHT));

        assertThat(journal.getPainRecords()).hasSize(2);
        assertThat(journal.getPainRecords()).allMatch(r -> r.getJournal() == journal);
    }

    @Test
    @DisplayName("addAttachment는 attachment에 journal 양방향 연관을 설정한다")
    void addAttachment_setsJournalReference() {
        WorkoutJournal journal = buildJournal();
        JournalAttachment attachment = JournalAttachment.builder()
                .imageUrl("https://cdn.ttamtam.app/img.jpg")
                .displayOrder(0)
                .build();

        journal.addAttachment(attachment);

        assertThat(attachment.getJournal()).isSameAs(journal);
    }

    @Test
    @DisplayName("addExercise는 exercises 리스트에 추가하고 exercise에 journal을 설정한다")
    void addExercise_addsToListAndSetsJournal() {
        WorkoutJournal journal = buildJournal();
        WorkoutExercise exercise = WorkoutExercise.builder()
                .exerciseName("스쿼트")
                .displayOrder(1)
                .build();

        journal.addExercise(exercise);

        assertThat(journal.getExercises()).hasSize(1);
        assertThat(journal.getExercises().get(0)).isSameAs(exercise);
        assertThat(exercise.getJournal()).isSameAs(journal);
    }

    @Test
    @DisplayName("postCondition이 null이면 isCompleted()는 false를 반환한다")
    void isCompleted_withoutPostCondition_returnsFalse() {
        WorkoutJournal journal = buildJournal();

        assertThat(journal.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("postCondition이 설정되면 isCompleted()는 true를 반환한다")
    void isCompleted_withPostCondition_returnsTrue() {
        WorkoutJournal journal = buildJournal();
        JournalPostCondition post = JournalPostCondition.of(journal, validPostConditionDto());

        journal.setPostCondition(post);

        assertThat(journal.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("setPreCondition은 양방향 연관을 설정한다")
    void setPreCondition_setsBidirectional() {
        WorkoutJournal journal = buildJournal();
        JournalPreCondition pre = JournalPreCondition.of(journal, new PreConditionDto(5, 7, 6, 4, 8));

        journal.setPreCondition(pre);

        assertThat(journal.getPreCondition()).isSameAs(pre);
        assertThat(pre.getJournal()).isSameAs(journal);
    }

    @Test
    @DisplayName("setPostCondition은 양방향 연관을 설정한다")
    void setPostCondition_setsBidirectional() {
        WorkoutJournal journal = buildJournal();
        JournalPostCondition post = JournalPostCondition.of(journal, validPostConditionDto());

        journal.setPostCondition(post);

        assertThat(journal.getPostCondition()).isSameAs(post);
        assertThat(post.getJournal()).isSameAs(journal);
    }

    @Test
    @DisplayName("빌더에 startedAt을 전달하면 해당 값으로 저장된다")
    void builder_withStartedAt_setsStartedAt() {
        LocalDateTime startedAt = LocalDateTime.of(2026, 4, 20, 9, 0);
        WorkoutJournal journal = WorkoutJournal.builder()
                .authorId(1L)
                .workoutDate(LocalDate.of(2026, 4, 20))
                .startedAt(startedAt)
                .build();

        assertThat(journal.getStartedAt()).isEqualTo(startedAt);
    }

    @Test
    @DisplayName("recordDuration(180) 호출 후 totalDurationSeconds는 180이다")
    void recordDuration_storesDuration() {
        WorkoutJournal journal = buildJournal();

        journal.recordDuration(180);

        assertThat(journal.getTotalDurationSeconds()).isEqualTo(180);
    }
}
