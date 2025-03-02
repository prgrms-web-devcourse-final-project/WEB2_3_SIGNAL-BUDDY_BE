package org.programmers.signalbuddyfinal.domain.auth.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AuthErrorCode implements ErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"14000", "다시 로그인 해주세요."),
    NOT_MATCH_AUTH_CODE(HttpStatus.BAD_REQUEST, "14001","인증 코드가 일치하지 않습니다."),
    INVALID_AUTH_CODE(HttpStatus.UNAUTHORIZED,"14002", "인증 코드가 유효하지 않습니다."),
    EMAIL_VERIFICATION_REQUIRED(HttpStatus.BAD_REQUEST, "14003", "이메일 본인 인증이 필요합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
