package com.example.zero.healthcare.Entity.journal;

import com.example.zero.healthcare.dto.journal.PostConditionDto;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "journal_post_condition")
@Getter
@NoArgsConstructor
public class JournalPostCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id", unique = true, nullable = false)
    private WorkoutJournal journal;

    @Column(name = "joint_muscle_pain")
    private Integer jointMusclePain;

    @Column(name = "intensity_fit")
    private Integer intensityFit;

    @Column(name = "goal_achieved")
    private Integer goalAchieved;

    @Column(name = "dizziness")
    private Integer dizziness;

    @Column(name = "mood")
    private Integer mood;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    public static JournalPostCondition of(WorkoutJournal journal, PostConditionDto dto) {
        JournalPostCondition post = new JournalPostCondition();
        post.journal = journal;
        post.jointMusclePain = dto.getJointMusclePain();
        post.intensityFit = dto.getIntensityFit();
        post.goalAchieved = dto.getGoalAchieved();
        post.dizziness = dto.getDizziness();
        post.mood = dto.getMood();
        post.recordedAt = LocalDateTime.now();
        return post;
    }

    public void update(PostConditionDto dto) {
        this.jointMusclePain = dto.getJointMusclePain();
        this.intensityFit = dto.getIntensityFit();
        this.goalAchieved = dto.getGoalAchieved();
        this.dizziness = dto.getDizziness();
        this.mood = dto.getMood();
    }

    void setJournal(WorkoutJournal journal) {
        this.journal = journal;
    }
}
