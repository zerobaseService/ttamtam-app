package com.example.zero.healthcare.dto.journal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateJournalRequest {

    @NotNull @PastOrPresent
    private LocalDate workoutDate;

    private Long folderId;

    @NotNull @Valid
    private PreConditionDto preCondition;

    @Valid
    private List<PainRecordDto> painRecords;

    @AssertTrue(message = "painRecords cannot contain duplicate bodyPart+side")
    public boolean isPainRecordsUnique() {
        if (painRecords == null) return true;
        long distinct = painRecords.stream()
                .map(r -> r.getBodyPart() + "_" + r.getSide())
                .distinct().count();
        return distinct == painRecords.size();
    }
}
