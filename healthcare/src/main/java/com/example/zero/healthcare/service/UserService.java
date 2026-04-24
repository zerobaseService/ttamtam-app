package com.example.zero.healthcare.service;

import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.dto.GoogleLoginRequest;
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
    public User loginOrRegister(GoogleLoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .map(existing -> {
                    existing.setIdToken(request.getIdToken());
                    return existing;
                })
                .orElseGet(() -> userRepository.save(
                        new User(request.getEmail(), request.getIdToken(), request.getNickname())
                ));
    }
}
