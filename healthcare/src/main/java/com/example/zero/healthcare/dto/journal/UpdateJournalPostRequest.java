package com.example.zero.healthcare.dto.journal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdateJournalPostRequest {

    @NotNull @Valid
    private PostConditionDto postCondition;

    @Valid
    private List<PainRecordDto> painRecords;

    @Size(max = 5000)
    private String content;

    @Size(max = 5, message = "이미지는 최대 5개까지 첨부 가능합니다.")
    private List<@URL @Size(max = 1024) String> imageUrls;
}
