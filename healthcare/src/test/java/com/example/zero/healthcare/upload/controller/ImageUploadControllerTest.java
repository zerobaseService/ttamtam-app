package com.example.zero.healthcare.upload.controller;

import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.auth.JwtTokenProvider;
import com.example.zero.healthcare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ImageUploadControllerTest {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void uploadProps(DynamicPropertyRegistry registry) {
        registry.add("app.upload.dir", () -> tempDir.toString());
        registry.add("app.upload.url-prefix", () -> "/static/journal-images");
        registry.add("app.upload.base-url", () -> "http://localhost:8080");
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    private String bearerToken;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(
                new User("upload-" + UUID.randomUUID() + "@test.com", "token", "tester"));
        bearerToken = "Bearer " + jwtTokenProvider.generateToken(user.getId());
    }

    @Test
    @DisplayName("유효한 이미지 업로드 시 200과 imageUrl을 반환한다")
    void upload_validRequest_returns200WithUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "fake-image".getBytes());

        mockMvc.perform(multipart("/api/uploads/journal-images")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.imageUrl").exists());
    }

    @Test
    @DisplayName("인증 없이 호출하면 401을 반환한다")
    void upload_noAuth_returns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "fake-image".getBytes());

        mockMvc.perform(multipart("/api/uploads/journal-images")
                        .file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("지원하지 않는 콘텐츠 타입이면 400을 반환한다")
    void upload_unsupportedContentType_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "data".getBytes());

        mockMvc.perform(multipart("/api/uploads/journal-images")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isBadRequest());
    }
}
