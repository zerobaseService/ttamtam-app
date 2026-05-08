package com.example.zero.healthcare.dto.journal;

import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class JournalDetailDto {

    private final Long journalId;
    private final Long folderId;
    private final LocalDate workoutDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime startedAt;
    private final Integer totalDurationSeconds;

    private final PreConditionResponseDto preCondition;
    private final PostConditionResponseDto postCondition;

    private final String content;
    private final List<PainRecordResponseDto> painRecords;
    private final List<AttachmentDto> attachments;
    private final List<ExerciseResponseDto> exercises;

    public JournalDetailDto(WorkoutJournal journal) {
        this.journalId = journal.getId();
        this.folderId = journal.getFolderId();
        this.workoutDate = journal.getWorkoutDate();
        this.createdAt = journal.getCreatedAt();
        this.startedAt = journal.getStartedAt();
        this.totalDurationSeconds = journal.getTotalDurationSeconds();

        this.preCondition = journal.getPreCondition() != null
                ? new PreConditionResponseDto(journal.getPreCondition()) : null;
        this.postCondition = journal.getPostCondition() != null
                ? new PostConditionResponseDto(journal.getPostCondition()) : null;

        this.content = journal.getContent();
        this.painRecords = journal.getPainRecords().stream()
                .map(PainRecordResponseDto::new)
                .collect(Collectors.toList());
        this.attachments = journal.getAttachments().stream()
                .map(AttachmentDto::new)
                .collect(Collectors.toList());
        this.exercises = journal.getExercises().stream()
                .map(ExerciseResponseDto::new)
                .collect(Collectors.toList());
    }
}
