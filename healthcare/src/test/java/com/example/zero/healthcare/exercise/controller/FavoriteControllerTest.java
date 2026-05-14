package com.example.zero.healthcare.exercise.controller;

import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.Entity.exercise.ExerciseMaster;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FavoriteControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private ExerciseMasterRepository exerciseRepository;
    @Autowired private UserExerciseFavoriteRepository favoriteRepository;

    private Long userId;
    private String bearerToken;
    private String exerciseId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(new User(UUID.randomUUID() + "@test.com", "token", "tester"));
        userId = user.getId();
        bearerToken = "Bearer " + jwtTokenProvider.generateToken(userId);

        ExerciseMaster e = new ExerciseMaster();
        exerciseId = "FavCtrl_" + UUID.randomUUID().toString().substring(0, 8);
        e.setId(exerciseId);
        e.setName("Favorite Control Test");
        e.setLevel("beginner");
        e.setCategory("strength");
        e.setEquipment("barbell");
        exerciseRepository.save(e);
    }

    @Test
    @DisplayName("POST /api/exercises/{id}/favorite — 204, DB row 생성됨")
    void addFavorite_validExercise_returns204AndPersists() throws Exception {
        mockMvc.perform(post("/api/exercises/" + exerciseId + "/favorite")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isNoContent());

        assertThat(favoriteRepository.existsByIdUserIdAndIdExerciseId(userId, exerciseId)).isTrue();
    }

    @Test
    @DisplayName("POST /api/exercises/{id}/favorite (이미 즐겨찾기) — 204 (멱등)")
    void addFavorite_alreadyFavorited_returns204Idempotent() throws Exception {
        mockMvc.perform(post("/api/exercises/" + exerciseId + "/favorite")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken));

        mockMvc.perform(post("/api/exercises/" + exerciseId + "/favorite")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/exercises/UNKNOWN/favorite — 404, EXERCISE_NOT_FOUND")
    void addFavorite_unknownExercise_returns404() throws Exception {
        mockMvc.perform(post("/api/exercises/UNKNOWN_EXERCISE_XYZ/favorite")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EXERCISE_NOT_FOUND"));
    }

    @Test
    @DisplayName("DELETE /api/exercises/{id}/favorite — 204, DB row 제거됨")
    void removeFavorite_existing_returns204AndRemoves() throws Exception {
        mockMvc.perform(post("/api/exercises/" + exerciseId + "/favorite")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken));

        mockMvc.perform(delete("/api/exercises/" + exerciseId + "/favorite")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isNoContent());

        assertThat(favoriteRepository.existsByIdUserIdAndIdExerciseId(userId, exerciseId)).isFalse();
    }

    @Test
    @DisplayName("DELETE /api/exercises/{id}/favorite (없는 즐겨찾기) — 204 (멱등)")
    void removeFavorite_notFavorited_returns204Idempotent() throws Exception {
        mockMvc.perform(delete("/api/exercises/" + exerciseId + "/favorite")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/me/exercises/favorites — 200, 본인 즐겨찾기 ID 배열 반환")
    void listFavorites_returns200WithOwnIds() throws Exception {
        mockMvc.perform(post("/api/exercises/" + exerciseId + "/favorite")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken));

        mockMvc.perform(get("/api/me/exercises/favorites")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value(exerciseId));
    }

    @Test
    @DisplayName("미인증 호출 시 401을 반환한다")
    void favorite_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/exercises/" + exerciseId + "/favorite"))
                .andExpect(status().isUnauthorized());
    }
}
