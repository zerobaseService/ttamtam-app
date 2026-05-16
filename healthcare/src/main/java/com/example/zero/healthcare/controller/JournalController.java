package com.example.zero.healthcare.controller;

import com.example.zero.healthcare.dto.common.ApiResponse;
import com.example.zero.healthcare.dto.journal.CompleteJournalRequest;
import com.example.zero.healthcare.dto.journal.CreateJournalRequest;
import com.example.zero.healthcare.dto.journal.CreateJournalResponse;
import com.example.zero.healthcare.dto.journal.JournalDetailDto;
import com.example.zero.healthcare.dto.journal.JournalSummaryDto;
import com.example.zero.healthcare.dto.journal.UpdateJournalRequest;
import com.example.zero.healthcare.service.JournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @Operation(summary = "운동 완료 — workoutDate로 PRE 일지 lookup 후 update, 없으면 insert (upsert)")
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<JournalDetailDto>> complete(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CompleteJournalRequest request) {
        JournalDetailDto data = journalService.completeByLookup(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "사용자 일지 목록 조회 (date/from+to 날짜 필터, folderId 또는 unfiled=true 폴더 필터 가능. folderId와 unfiled 동시 지정 불가)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<JournalSummaryDto>>> getMyJournals(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false, defaultValue = "false") boolean unfiled) {
        List<JournalSummaryDto> data = journalService.getMyJournals(userId, date, from, to, folderId, unfiled);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "일지 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JournalDetailDto>> getJournalDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        JournalDetailDto data = journalService.getJournalDetail(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "일지 수정 (PATCH — null 필드는 변경 없음)")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<JournalDetailDto>> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateJournalRequest request) {
        JournalDetailDto data = journalService.updateJournal(userId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "일지 soft-delete (작성자 본인만)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        journalService.deleteJournal(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
