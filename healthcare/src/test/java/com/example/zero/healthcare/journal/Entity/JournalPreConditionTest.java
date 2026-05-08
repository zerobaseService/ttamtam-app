package com.example.zero.healthcare.journal.Entity;

import com.example.zero.healthcare.Entity.journal.JournalPreCondition;
import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import com.example.zero.healthcare.dto.journal.PreConditionDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class JournalPreConditionTest {

    private WorkoutJournal buildJournal() {
        return WorkoutJournal.builder()
                .authorId(1L)
                .workoutDate(LocalDate.of(2026, 4, 20))
                .build();
    }

    private PreConditionDto validDto() {
        return new PreConditionDto(5, 7, 6, 4, 8);
    }

    @Test
    @DisplayName("of() 팩토리는 모든 필드를 올바르게 설정한다")
    void of_setsAllFields() {
        WorkoutJournal journal = buildJournal();

        JournalPreCondition pre = JournalPreCondition.of(journal, validDto());

        assertThat(pre.getJointMusclePain()).isEqualTo(5);
        assertThat(pre.getSleepHours()).isEqualTo(7);
        assertThat(pre.getSleepQuality()).isEqualTo(6);
        assertThat(pre.getPreviousFatigue()).isEqualTo(4);
        assertThat(pre.getOverallCondition()).isEqualTo(8);
    }

    @Test
    @DisplayName("of() 팩토리는 journal 참조를 설정한다")
    void of_setsJournalReference() {
        WorkoutJournal journal = buildJournal();

        JournalPreCondition pre = JournalPreCondition.of(journal, validDto());

        assertThat(pre.getJournal()).isSameAs(journal);
    }
}
