package org.programmers.signalbuddyfinal.global.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GlobalErrorCode implements ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "09000", "잘못된 요청입니다."),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "09001", "알 수 없는 에러가 발생했습니다. 관리자에게 문의하세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
