package com.nsuslab.supports.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "일시적인 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "존재하지 않는 요청입니다."),
    CONFLICT(HttpStatus.CONFLICT, HttpStatus.CONFLICT.getReasonPhrase(), "이미 존재하는 리소스입니다."),
    
    // 인증 관련 에러
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS", "잘못된 인증 정보입니다."),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND", "존재하지 않는 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "DUPLICATE_EMAIL", "이미 존재하는 이메일입니다."),
    ACCOUNT_LOCKED(HttpStatus.CONFLICT, "ACCOUNT_LOCKED", "계정이 잠겨있습니다. 잠시 후 다시 시도해주세요."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "TOKEN_NOT_FOUND", "토큰을 찾을 수 없습니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", "요청 횟수 제한을 초과했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
