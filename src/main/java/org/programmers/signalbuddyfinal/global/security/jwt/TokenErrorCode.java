package org.programmers.signalbuddyfinal.global.security.jwt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TokenErrorCode implements ErrorCode {

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 13000, "유효하지 않은 토큰 입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, 13001, "만료된 토큰입니다."),
    ACCESS_TOKEN_NOT_EXIST(HttpStatus.UNAUTHORIZED,13002,"액세스 토큰이 존재하지 않습니다."),
    REFRESH_TOKEN_NOT_EXIST(HttpStatus.UNAUTHORIZED,13003,"리프레시 토큰이 존재하지 않습니다.");

    private HttpStatus httpStatus;
    private int code;
    private String message;
}
