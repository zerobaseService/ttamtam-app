package com.example.zero.healthcare.dto.journal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class SetDto {

    @Min(1)
    private Integer setNumber;

    @Min(0)
    private Integer reps;

    @DecimalMin("0.0")
    private BigDecimal weightKg;
}
