package com.example.zero.healthcare.exercise.controller;

import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.Entity.exercise.ExerciseMaster;
import com.example.zero.healthcare.Entity.exercise.UserExerciseFavorite;
import com.example.zero.healthcare.Entity.exercise.UserExerciseFavoriteId;
import com.example.zero.healthcare.auth.JwtTokenProvider;
import com.example.zero.healthcare.repository.ExerciseMasterRepository;
import com.example.zero.healthcare.repository.UserExerciseFavoriteRepository;
import com.example.zero.healthcare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExerciseMasterControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private ExerciseMasterRepository exerciseRepository;
    @Autowired private UserExerciseFavoriteRepository favoriteRepository;

    private String bearerToken;
    private Long userId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(new User(UUID.randomUUID() + "@test.com", "token", "tester"));
        userId = user.getId();
        bearerToken = "Bearer " + jwtTokenProvider.generateToken(userId);

        ExerciseMaster e = new ExerciseMaster();
        e.setId("Ctrl_Bench_" + UUID.randomUUID().toString().substring(0, 8));
        e.setName("Bench Press");
        e.setKoreanName("벤치 프레스");
        e.setLevel("beginner");
        e.setCategory("strength");
        e.setEquipment("barbell");
        exerciseRepository.save(e);
    }

    @Test
    @DisplayName("GET /api/exercises/all — 200 및 배열 응답")
    void getAll_authenticated_returns200WithArray() throws Exception {
        mockMvc.perform(get("/api/exercises/all")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/exercises/{id} — 200, name/koreanName 포함, images 필드 없음")
    void getById_existing_returns200WithKoreanName() throws Exception {
        ExerciseMaster saved = exerciseRepository.findAll().stream()
                .filter(e -> "Bench Press".equals(e.getName()))
                .findFirst().orElseThrow();

        mockMvc.perform(get("/api/exercises/" + saved.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Bench Press"))
                .andExpect(jsonPath("$.data.koreanName").value("벤치 프레스"))
                .andExpect(jsonPath("$.data.images").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/exercises/UNKNOWN — 404, EXERCISE_NOT_FOUND")
    void getById_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/exercises/UNKNOWN_XYZ_12345")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EXERCISE_NOT_FOUND"));
    }

    @Test
    @DisplayName("미인증 호출 시 401을 반환한다")
    void getAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/exercises/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("즐겨찾기 등록된 운동이 응답 배열 맨 앞에 오고 isFavorite=true다")
    void getAll_authenticated_favoriteFirst() throws Exception {
        ExerciseMaster favExercise = new ExerciseMaster();
        favExercise.setId("Fav_Squat_" + UUID.randomUUID().toString().substring(0, 8));
        favExercise.setName("Squat");
        favExercise.setKoreanName("가 스쿼트");  // 가나다순 첫 번째
        favExercise.setLevel("beginner");
        favExercise.setCategory("strength");
        favExercise.setEquipment("barbell");
        exerciseRepository.save(favExercise);

        UserExerciseFavorite favorite = new UserExerciseFavorite();
        favorite.setId(new UserExerciseFavoriteId(userId, favExercise.getId()));
        favorite.setExercise(favExercise);
        favorite.setCreatedAt(LocalDateTime.now());
        favoriteRepository.save(favorite);

        mockMvc.perform(get("/api/exercises/all")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].isFavorite").value(true));
    }
}
