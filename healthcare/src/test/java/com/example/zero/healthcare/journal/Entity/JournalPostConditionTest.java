package com.example.zero.healthcare.journal.Entity;

import com.example.zero.healthcare.Entity.journal.JournalPostCondition;
import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import com.example.zero.healthcare.dto.journal.PostConditionDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class JournalPostConditionTest {

    private WorkoutJournal buildJournal() {
        return WorkoutJournal.builder()
                .authorId(1L)
                .workoutDate(LocalDate.of(2026, 4, 20))
                .build();
    }

    private PostConditionDto validDto() {
        PostConditionDto dto = new PostConditionDto();
        dto.setJointMusclePain(6);
        dto.setIntensityFit(7);
        dto.setGoalAchieved(8);
        dto.setDizziness(2);
        dto.setMood(9);
        return dto;
    }

    @Test
    @DisplayName("of() 팩토리는 모든 필드를 올바르게 설정하고 recordedAt을 기록한다")
    void of_setsAllFieldsAndRecordedAt() {
        WorkoutJournal journal = buildJournal();

        JournalPostCondition post = JournalPostCondition.of(journal, validDto());

        assertThat(post.getJointMusclePain()).isEqualTo(6);
        assertThat(post.getIntensityFit()).isEqualTo(7);
        assertThat(post.getGoalAchieved()).isEqualTo(8);
        assertThat(post.getDizziness()).isEqualTo(2);
        assertThat(post.getMood()).isEqualTo(9);
        assertThat(post.getRecordedAt()).isNotNull();
    }

    @Test
    @DisplayName("of() 팩토리는 journal 참조를 설정한다")
    void of_setsJournalReference() {
        WorkoutJournal journal = buildJournal();

        JournalPostCondition post = JournalPostCondition.of(journal, validDto());

        assertThat(post.getJournal()).isSameAs(journal);
    }
}
