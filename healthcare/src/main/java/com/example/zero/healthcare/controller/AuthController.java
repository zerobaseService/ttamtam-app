package com.example.zero.healthcare.controller;


import com.example.zero.healthcare.dto.GoogleLoginRequest;
import com.example.zero.healthcare.dto.UserResponse;
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
    public AuthController(UserService userService){
        this.userService = userService;
    }
    @PostMapping("/google")
    public ResponseEntity<UserResponse> register(@RequestBody GoogleLoginRequest request){


        System.out.println("토큰:" + request.getIdToken());
        System.out.println("이메일: " + request.getEmail());
        System.out.println("닉네임: " + request.getNickname());

        userService.loginOrRegister(request);
        UserResponse response = new UserResponse(true,"토큰완료",1L);
        return ResponseEntity.ok(response);
    }
}
