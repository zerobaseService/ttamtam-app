package com.example.zero.healthcare.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private boolean success;
    private String message;
    private Long userId;
    private String accessToken;
}
