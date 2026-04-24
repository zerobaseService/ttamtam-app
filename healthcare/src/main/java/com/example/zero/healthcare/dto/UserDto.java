package com.example.zero.healthcare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UserDto {

    @Schema(description = "사용자 ID", example = "1")
    private final Long userId;

    @Schema(description = "JWT 액세스 토큰")
    private final String accessToken;

    public UserDto(Long userId, String accessToken) {
        this.userId = userId;
        this.accessToken = accessToken;
    }
}
