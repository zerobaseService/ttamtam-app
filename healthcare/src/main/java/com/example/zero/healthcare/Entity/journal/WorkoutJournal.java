package com.example.zero.healthcare.Entity.journal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
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

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "total_duration_seconds")
    private Integer totalDurationSeconds;

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

    @OneToOne(mappedBy = "journal", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private JournalPreCondition preCondition;

    @OneToOne(mappedBy = "journal", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private JournalPostCondition postCondition;

    @OneToMany(mappedBy = "journal", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    @OrderBy("displayOrder ASC")
    private List<WorkoutExercise> exercises = new ArrayList<>();

    @OneToMany(mappedBy = "journal", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<JournalPainRecord> painRecords = new ArrayList<>();

    @OneToMany(mappedBy = "journal", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @jakarta.persistence.OrderBy("displayOrder ASC")
    private List<JournalAttachment> attachments = new ArrayList<>();

    @Builder
    public WorkoutJournal(Long folderId, Long authorId, LocalDate workoutDate, LocalDateTime startedAt) {
        this.folderId = folderId;
        this.authorId = authorId;
        this.workoutDate = workoutDate;
        this.startedAt = startedAt;
    }

    public void recordDuration(Integer seconds) {
        this.totalDurationSeconds = seconds;
    }

    public void setPreCondition(JournalPreCondition pre) {
        this.preCondition = pre;
        pre.setJournal(this);
    }

    public void setPostCondition(JournalPostCondition post) {
        this.postCondition = post;
        post.setJournal(this);
    }

    public boolean isCompleted() {
        return postCondition != null;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void addExercise(WorkoutExercise exercise) {
        exercises.add(exercise);
        exercise.setJournal(this);
    }

    public void addPainRecord(JournalPainRecord record) {
        painRecords.add(record);
        record.setJournal(this);
    }

    public void addAttachment(JournalAttachment attachment) {
        attachments.add(attachment);
        attachment.setJournal(this);
    }

    public void replacePainRecords(PainTiming timing, List<JournalPainRecord> next) {
        painRecords.removeIf(r -> r.getTiming() == timing);
        for (JournalPainRecord record : next) {
            record.setJournal(this);
            painRecords.add(record);
        }
    }

    public void replaceAttachments(List<String> urls) {
        attachments.clear();
        for (int i = 0; i < urls.size(); i++) {
            JournalAttachment attachment = JournalAttachment.builder()
                    .imageUrl(urls.get(i))
                    .displayOrder(i)
                    .build();
            attachment.setJournal(this);
            attachments.add(attachment);
        }
    }

    public void touch() {
        this.updatedAt = java.time.LocalDateTime.now();
    }
}
