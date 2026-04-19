package com.example.zero.healthcare.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private boolean success;
    private String message;
    private Long userId;

    public UserResponse(){

    }
    public UserResponse(boolean success,String message,Long userId){
        this.success = success;
        this.message = message;
        this.userId = userId;
    }
}
