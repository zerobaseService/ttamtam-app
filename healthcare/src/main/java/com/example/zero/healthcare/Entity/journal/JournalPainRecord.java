package com.example.zero.healthcare.Entity.journal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "journal_pain_record",
        uniqueConstraints = @UniqueConstraint(columnNames = {"journal_id", "timing", "body_part", "side"})
)
@Getter
@NoArgsConstructor
public class JournalPainRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id", nullable = false)
    private WorkoutJournal journal;

    @Enumerated(EnumType.STRING)
    @Column(name = "timing", nullable = false, length = 10)
    private PainTiming timing;

    @Enumerated(EnumType.STRING)
    @Column(name = "body_part", nullable = false, length = 30)
    private BodyPart bodyPart;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, length = 10)
    private BodySide side;

    @Column(name = "pain_level", nullable = false)
    private Integer painLevel;

    @Column(name = "pain_reason", length = 1000)
    private String painReason;

    @Builder
    public JournalPainRecord(PainTiming timing, BodyPart bodyPart, BodySide side, Integer painLevel, String painReason) {
        this.timing = timing;
        this.bodyPart = bodyPart;
        this.side = side;
        this.painLevel = painLevel;
        this.painReason = painReason;
    }

    public static JournalPainRecord fromDto(PainTiming timing, com.example.zero.healthcare.dto.journal.PainRecordDto dto) {
        return JournalPainRecord.builder()
                .timing(timing)
                .bodyPart(BodyPart.valueOf(dto.getBodyPart()))
                .side(BodySide.valueOf(dto.getSide()))
                .painLevel(dto.getPainLevel())
                .painReason(dto.getPainReason())
                .build();
    }

    void setJournal(WorkoutJournal journal) {
        this.journal = journal;
    }
}
