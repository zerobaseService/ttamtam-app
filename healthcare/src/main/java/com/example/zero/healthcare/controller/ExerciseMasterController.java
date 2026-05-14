package com.example.zero.healthcare.controller;

import com.example.zero.healthcare.dto.common.ApiResponse;
import com.example.zero.healthcare.dto.exercise.ExerciseDetailDto;
import com.example.zero.healthcare.dto.exercise.ExerciseSummaryDto;
import com.example.zero.healthcare.service.ExerciseMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Exercise", description = "운동 종목 마스터 API")
@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseMasterController {

    private final ExerciseMasterService exerciseMasterService;

    @Operation(summary = "운동 종목 전체 목록 조회")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ExerciseSummaryDto>>> getAll(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(exerciseMasterService.getAll(userId)));
    }

    @Operation(summary = "운동 종목 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExerciseDetailDto>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(exerciseMasterService.getById(id)));
    }
}
