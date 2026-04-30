package com.example.zero.healthcare.journal.Entity;

import com.example.zero.healthcare.Entity.journal.BodyPart;
import com.example.zero.healthcare.Entity.journal.BodySide;
import com.example.zero.healthcare.Entity.journal.JournalAttachment;
import com.example.zero.healthcare.Entity.journal.JournalPainRecord;
import com.example.zero.healthcare.Entity.journal.PainTiming;
import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkoutJournalTest {

    private WorkoutJournal buildJournal() {
        return WorkoutJournal.builder()
                .authorId(1L)
                .workoutDate(LocalDate.of(2026, 4, 20))
                .preJointMusclePain(5)
                .preSleepHours(7)
                .preSleepQuality(6)
                .prePreviousFatigue(4)
                .preOverallCondition(8)
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

    @Test
    @DisplayName("workoutDate를 빌더에 전달하면 해당 날짜로 저장된다")
    void builder_withWorkoutDate_setsWorkoutDate() {
        LocalDate date = LocalDate.of(2026, 4, 20);
        WorkoutJournal journal = WorkoutJournal.builder()
                .authorId(1L)
                .workoutDate(date)
                .preJointMusclePain(5)
                .preSleepHours(7)
                .preSleepQuality(6)
                .prePreviousFatigue(4)
                .preOverallCondition(8)
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
    @DisplayName("신규 일지는 post 필드가 모두 null이고 postRecordedAt도 null이다")
    void newJournal_postFieldsAreNull() {
        WorkoutJournal journal = buildJournal();

        assertThat(journal.getPostJointMusclePain()).isNull();
        assertThat(journal.getPostIntensityFit()).isNull();
        assertThat(journal.getPostGoalAchieved()).isNull();
        assertThat(journal.getPostDizziness()).isNull();
        assertThat(journal.getPostMood()).isNull();
        assertThat(journal.getPostRecordedAt()).isNull();
        assertThat(journal.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("applyPostCondition은 post 필드를 세팅하고 postRecordedAt을 기록한다")
    void applyPostCondition_setsPostFieldsAndRecordedAt() {
        WorkoutJournal journal = buildJournal();

        journal.applyPostCondition(6, 7, 8, 2, 9, "오늘 컨디션 좋았다");

        assertThat(journal.getPostJointMusclePain()).isEqualTo(6);
        assertThat(journal.getPostIntensityFit()).isEqualTo(7);
        assertThat(journal.getPostGoalAchieved()).isEqualTo(8);
        assertThat(journal.getPostDizziness()).isEqualTo(2);
        assertThat(journal.getPostMood()).isEqualTo(9);
        assertThat(journal.getContent()).isEqualTo("오늘 컨디션 좋았다");
        assertThat(journal.getPostRecordedAt()).isNotNull();
    }

    @Test
    @DisplayName("pre 컨디션 없이 일지를 빌드하면 pre 필드가 모두 null이다")
    void builder_withoutPreFields_preFieldsAreNull() {
        WorkoutJournal journal = WorkoutJournal.builder()
                .authorId(1L)
                .workoutDate(LocalDate.of(2026, 4, 20))
                .build();

        assertThat(journal.getPreJointMusclePain()).isNull();
        assertThat(journal.getPreSleepHours()).isNull();
        assertThat(journal.getPreSleepQuality()).isNull();
        assertThat(journal.getPrePreviousFatigue()).isNull();
        assertThat(journal.getPreOverallCondition()).isNull();
    }

    @Test
    @DisplayName("이미지를 5개까지 첨부할 수 있다")
    void addAttachment_max5_ok() {
        WorkoutJournal journal = buildJournal();
        for (int i = 0; i < 5; i++) {
            journal.addAttachment(JournalAttachment.builder()
                    .imageUrl("https://cdn.ttamtam.app/img" + i + ".jpg")
                    .displayOrder(i)
                    .build());
        }
        assertThat(journal.getAttachments()).hasSize(5);
    }

    @Test
    @DisplayName("이미지 6개 이상 추가 시 IllegalStateException이 발생한다")
    void addAttachment_over5_throwsIllegalState() {
        WorkoutJournal journal = buildJournal();
        for (int i = 0; i < 5; i++) {
            journal.addAttachment(JournalAttachment.builder()
                    .imageUrl("https://cdn.ttamtam.app/img" + i + ".jpg")
                    .displayOrder(i)
                    .build());
        }
        JournalAttachment extra = JournalAttachment.builder()
                .imageUrl("https://cdn.ttamtam.app/extra.jpg")
                .displayOrder(5)
                .build();

        assertThatThrownBy(() -> journal.addAttachment(extra))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("5");
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
}
