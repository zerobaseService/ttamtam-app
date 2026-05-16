package com.example.zero.healthcare.dto.journal;

import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class JournalSummaryDto {

    private final Long journalId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate workoutDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime createdAt;
    private final boolean postRecorded;
    private final Integer preOverallCondition;
    private final String contentPreview;

    public JournalSummaryDto(WorkoutJournal journal) {
        this.journalId = journal.getId();
        this.workoutDate = journal.getWorkoutDate();
        this.createdAt = journal.getCreatedAt();
        this.postRecorded = journal.isCompleted();
        this.preOverallCondition = journal.getPreCondition() != null
                ? journal.getPreCondition().getOverallCondition() : null;
        String c = journal.getContent();
        this.contentPreview = (c != null && c.length() > 100) ? c.substring(0, 100) : c;
    }
}
