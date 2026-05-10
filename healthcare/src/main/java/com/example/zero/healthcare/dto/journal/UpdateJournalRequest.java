package com.example.zero.healthcare.dto.journal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdateJournalRequest {

    @Valid
    private PreConditionDto preCondition;

    @Valid
    private PostConditionDto postCondition;

    @Valid
    private List<PainRecordDto> prePainRecords;

    @Valid
    private List<PainRecordDto> postPainRecords;

    @Size(max = 5000)
    private String content;

    @Valid
    @Size(max = 5)
    private List<@URL @Size(max = 1024) String> imageUrls;
}
