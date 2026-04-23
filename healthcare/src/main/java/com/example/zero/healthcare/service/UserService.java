package com.example.zero.healthcare.service;

import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.dto.GoogleLoginRequest;
import com.example.zero.healthcare.dto.UserDto;
import com.example.zero.healthcare.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDto loginOrRegister(GoogleLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .map(existing -> {
                    // 2. 이미 있다면 토큰만 갱신 (Update)
                    existing.setIdToken(request.getIdToken());
                    return existing;
                })
                .orElseGet(() -> userRepository.save(
                        new User(request.getEmail(), request.getIdToken(), request.getNickname())
                ));
        return new UserDto(user.getId());
    }
}
