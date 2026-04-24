package com.example.zero.healthcare.dto;

import lombok.Getter;

@Getter
public class UserDto {
    private final Long userId;
    private final String accessToken;

    public UserDto(Long userId, String accessToken) {
        this.userId = userId;
        this.accessToken = accessToken;
    }
}
