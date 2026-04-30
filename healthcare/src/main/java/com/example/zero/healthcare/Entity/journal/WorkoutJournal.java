package com.example.zero.healthcare.Entity.journal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_journal",
        indexes = @Index(name = "idx_journal_author_date", columnList = "author_id, workout_date, created_at"))
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE workout_journal SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class WorkoutJournal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folder_id")
    private Long folderId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "workout_date", nullable = false)
    private LocalDate workoutDate;

    @Column(name = "pre_joint_muscle_pain")
    private Integer preJointMusclePain;

    @Column(name = "pre_sleep_hours")
    private Integer preSleepHours;

    @Column(name = "pre_sleep_quality")
    private Integer preSleepQuality;

    @Column(name = "pre_previous_fatigue")
    private Integer prePreviousFatigue;

    @Column(name = "pre_overall_condition")
    private Integer preOverallCondition;

    @Column(name = "post_joint_muscle_pain")
    private Integer postJointMusclePain;

    @Column(name = "post_intensity_fit")
    private Integer postIntensityFit;

    @Column(name = "post_goal_achieved")
    private Integer postGoalAchieved;

    @Column(name = "post_dizziness")
    private Integer postDizziness;

    @Column(name = "post_mood")
    private Integer postMood;

    @Column(name = "post_recorded_at")
    private LocalDateTime postRecordedAt;

    @Column(name = "content", length = 5000)
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "journal", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<JournalPainRecord> painRecords = new ArrayList<>();

//    @OneToMany(mappedBy = "journal", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
//    @jakarta.persistence.OrderBy("displayOrder ASC")
//    private List<JournalAttachment> attachments = new ArrayList<>();

    @Builder
    public WorkoutJournal(Long folderId, Long authorId, LocalDate workoutDate,
                          Integer preJointMusclePain, Integer preSleepHours,
                          Integer preSleepQuality, Integer prePreviousFatigue,
                          Integer preOverallCondition) {
        this.folderId = folderId;
        this.authorId = authorId;
        this.workoutDate = workoutDate;
        this.preJointMusclePain = preJointMusclePain;
        this.preSleepHours = preSleepHours;
        this.preSleepQuality = preSleepQuality;
        this.prePreviousFatigue = prePreviousFatigue;
        this.preOverallCondition = preOverallCondition;
    }

    public void addPainRecord(JournalPainRecord record) {
        painRecords.add(record);
        record.setJournal(this);
    }

//    public void addAttachment(JournalAttachment attachment) {
//        if (attachments.size() >= 5) {
//            throw new IllegalStateException("이미지는 최대 5개까지 첨부 가능합니다.");
//        }
//        attachments.add(attachment);
//        attachment.setJournal(this);
//    }

    public void applyPostCondition(Integer postJointMusclePain, Integer postIntensityFit,
                                   Integer postGoalAchieved, Integer postDizziness,
                                   Integer postMood, String content) {
        this.postJointMusclePain = postJointMusclePain;
        this.postIntensityFit = postIntensityFit;
        this.postGoalAchieved = postGoalAchieved;
        this.postDizziness = postDizziness;
        this.postMood = postMood;
        this.content = content;
        this.postRecordedAt = LocalDateTime.now();
    }
}
