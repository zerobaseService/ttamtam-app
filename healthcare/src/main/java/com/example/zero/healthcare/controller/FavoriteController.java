package com.example.zero.healthcare.controller;

import com.example.zero.healthcare.dto.common.ApiResponse;
import com.example.zero.healthcare.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Favorite", description = "운동 즐겨찾기 API")
@RestController
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "즐겨찾기 추가 (멱등)")
    @PostMapping("/api/exercises/{id}/favorite")
    public ResponseEntity<Void> add(
            @AuthenticationPrincipal Long userId,
            @PathVariable String id) {
        favoriteService.add(userId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "즐겨찾기 해제 (멱등)")
    @DeleteMapping("/api/exercises/{id}/favorite")
    public ResponseEntity<Void> remove(
            @AuthenticationPrincipal Long userId,
            @PathVariable String id) {
        favoriteService.remove(userId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 즐겨찾기 목록 조회")
    @GetMapping("/api/me/exercises/favorites")
    public ResponseEntity<ApiResponse<List<String>>> list(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(favoriteService.listIds(userId)));
    }
}
