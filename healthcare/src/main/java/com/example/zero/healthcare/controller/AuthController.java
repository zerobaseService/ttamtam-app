package com.example.zero.healthcare.controller;

import com.example.zero.healthcare.dto.GoogleLoginRequest;
import com.example.zero.healthcare.dto.UserDto;
import com.example.zero.healthcare.dto.common.ApiResponse;
import com.example.zero.healthcare.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<UserDto>> register(@RequestBody GoogleLoginRequest request) {
        UserDto userDto = userService.loginOrRegister(request);
        return ResponseEntity.ok(ApiResponse.ok(userDto, "토큰완료"));
    }
}
