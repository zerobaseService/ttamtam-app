package com.example.zero.healthcare.Entity.journal;

import com.example.zero.healthcare.dto.journal.PreConditionDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "journal_pre_condition")
@Getter
@NoArgsConstructor
public class JournalPreCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id", unique = true, nullable = false)
    private WorkoutJournal journal;

    @Column(name = "joint_muscle_pain")
    private Integer jointMusclePain;

    @Column(name = "sleep_hours")
    private Integer sleepHours;

    @Column(name = "sleep_quality")
    private Integer sleepQuality;

    @Column(name = "previous_fatigue")
    private Integer previousFatigue;

    @Column(name = "overall_condition")
    private Integer overallCondition;

    public static JournalPreCondition of(WorkoutJournal journal, PreConditionDto dto) {
        JournalPreCondition pre = new JournalPreCondition();
        pre.journal = journal;
        pre.jointMusclePain = dto.getJointMusclePain();
        pre.sleepHours = dto.getSleepHours();
        pre.sleepQuality = dto.getSleepQuality();
        pre.previousFatigue = dto.getPreviousFatigue();
        pre.overallCondition = dto.getOverallCondition();
        return pre;
    }

    void setJournal(WorkoutJournal journal) {
        this.journal = journal;
    }
}
