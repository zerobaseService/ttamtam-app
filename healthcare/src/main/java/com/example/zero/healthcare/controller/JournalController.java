package com.example.zero.healthcare.controller;

import com.example.zero.healthcare.dto.common.ApiResponse;
import com.example.zero.healthcare.dto.journal.CompleteJournalRequest;
import com.example.zero.healthcare.dto.journal.CreateJournalRequest;
import com.example.zero.healthcare.dto.journal.CreateJournalResponse;
import com.example.zero.healthcare.dto.journal.JournalDetailDto;
import com.example.zero.healthcare.dto.journal.JournalSummaryDto;
import com.example.zero.healthcare.dto.journal.UpdateJournalPostRequest;
import com.example.zero.healthcare.service.JournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Journal", description = "운동 일지 관련 API")
@RestController
@RequestMapping("/api/journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @Operation(summary = "컨디션 기록 새 운동 일지 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateJournalResponse>> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateJournalRequest request) {
        CreateJournalResponse data = journalService.createJournal(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(data));
    }

    @Operation(summary = "운동 후 기록 추가 (PATCH)")
    @PatchMapping("/{id}/post")
    public ResponseEntity<ApiResponse<JournalDetailDto>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJournalPostRequest request) {
        JournalDetailDto data = journalService.updatePostCondition(id, request);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "journalId 없이 운동 후 기록 완료 (lookup-based update)")
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<JournalDetailDto>> complete(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CompleteJournalRequest request) {
        JournalDetailDto data = journalService.completeByLookup(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "사용자 일지 목록 조회 (date 또는 from+to로 필터링 가능)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<JournalSummaryDto>>> getMyJournals(
            @RequestParam Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<JournalSummaryDto> data = journalService.getMyJournals(userId, date, from, to);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "일지 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JournalDetailDto>> getJournalDetail(@PathVariable Long id) {
        JournalDetailDto data = journalService.getJournalDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
