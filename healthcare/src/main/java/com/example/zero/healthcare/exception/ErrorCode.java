package com.example.zero.healthcare.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND, "FOLDER_NOT_FOUND", "폴더를 찾을 수 없습니다."),
    FOLDER_FULL(HttpStatus.CONFLICT, "FOLDER_FULL", "폴더 정원이 가득 찼습니다."),
    FOLDER_CLOSED(HttpStatus.BAD_REQUEST, "FOLDER_CLOSED", "닫힌 폴더입니다."),
    ALREADY_MEMBER(HttpStatus.BAD_REQUEST, "ALREADY_MEMBER", "이미 해당 폴더의 활성 멤버입니다."),
    ALREADY_LEFT(HttpStatus.BAD_REQUEST, "ALREADY_LEFT", "이미 나간 상태입니다."),
    INVALID_FOLDER_NAME(HttpStatus.BAD_REQUEST, "INVALID_FOLDER_NAME", "폴더명 형식 또는 길이가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "유효하지 않은 초대 토큰입니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "INVALID_CURSOR", "커서 값이 올바르지 않습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "해당 폴더에 대한 접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
