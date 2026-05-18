package com.example.zero.healthcare.dto.journal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PainRecordDto {

    private String bodyPart;

    private String side;

    @Schema(description = "통증 강도 (1~5)", example = "3")
    @Min(1) @Max(5)
    private Integer painLevel;

    @Schema(description = "통증이 생긴 이유 (옵션, 최대 1000자)", example = "어깨가 결림")
    @Size(max = 1000)
    private String painReason;

    public static PainRecordDto of(String bodyPart, String side, Integer painLevel) {
        return new PainRecordDto(bodyPart, side, painLevel, null);
    }

    public static PainRecordDto of(String bodyPart, String side, Integer painLevel, String painReason) {
        return new PainRecordDto(bodyPart, side, painLevel, painReason);
    }
}
