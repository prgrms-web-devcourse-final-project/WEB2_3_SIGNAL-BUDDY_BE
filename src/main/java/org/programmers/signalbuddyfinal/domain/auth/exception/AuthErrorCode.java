package org.programmers.signalbuddyfinal.domain.auth.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AuthErrorCode implements ErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"14000", "다시 로그인 해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
