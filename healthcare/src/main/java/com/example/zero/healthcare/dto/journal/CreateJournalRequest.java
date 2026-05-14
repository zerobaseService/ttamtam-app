package com.example.zero.healthcare.dto.journal;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateJournalRequest {

    @NotNull @PastOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workoutDate;

    private Long folderId;

    @NotNull @Valid
    private PreConditionDto preCondition;

    @Valid
    private List<PainRecordDto> painRecords;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;

    @AssertTrue(message = "painRecords cannot contain duplicate bodyPart+side")
    public boolean isPainRecordsUnique() {
        if (painRecords == null) return true;
        long distinct = painRecords.stream()
                .map(r -> r.getBodyPart() + "_" + r.getSide())
                .distinct().count();
        return distinct == painRecords.size();
    }
}
