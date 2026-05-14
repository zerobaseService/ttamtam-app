package com.example.zero.healthcare.dto.journal;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CompleteJournalRequest {

    @NotNull @PastOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workoutDate;

    @NotNull @PastOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;

    @PositiveOrZero
    private Integer totalDurationSeconds;

    private Long folderId;

    @NotNull @Valid
    private PostConditionDto postCondition;

    @Valid
    private List<PainRecordDto> painRecords;

    @Valid
    private List<ExerciseDto> exercises;

    @Size(max = 5000)
    private String content;

    @Size(max = 20)
    private String workoutType;

    @Size(max = 5, message = "이미지는 최대 5개까지 첨부 가능합니다.")
    private List<@URL @Size(max = 1024) String> imageUrls;
}
