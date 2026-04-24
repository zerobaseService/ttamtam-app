package com.example.zero.healthcare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UserDto {

    @Schema(description = "사용자 ID", example = "1")
    private final Long userId;

    public UserDto(Long userId) {
        this.userId = userId;
    }
}
