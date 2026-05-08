package com.example.zero.healthcare.dto.journal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ExerciseDto {

    @NotBlank
    private String exerciseName;

    private Integer displayOrder;

    @Valid
    private List<SetDto> sets;
}
