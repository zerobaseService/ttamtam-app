package com.example.zero.healthcare.exception.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // === Common ===
    INVALID_REQUEST(400, "INVALID_REQUEST", "잘못된 요청입니다."),
    INTERNAL_ERROR(500, "INTERNAL_ERROR", "서버 오류가 발생했습니다."),
    DATA_INTEGRITY_VIOLATION(400, "INVALID_REQUEST", "데이터 제약 조건 위반입니다."),

    // === User ===
    USER_NOT_FOUND(404, "USER_NOT_FOUND", "존재하지 않는 사용자입니다."),

    // === Journal ===
    JOURNAL_NOT_FOUND(404, "JOURNAL_NOT_FOUND", "존재하지 않는 일지입니다."),
    POST_ALREADY_RECORDED(409, "POST_ALREADY_RECORDED", "이미 운동 후 기록이 완료된 일지입니다.");

    private final int status;
    private final String code;
    private final String message;
}
