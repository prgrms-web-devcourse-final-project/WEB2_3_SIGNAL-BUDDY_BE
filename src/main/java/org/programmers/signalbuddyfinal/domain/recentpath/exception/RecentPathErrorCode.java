package org.programmers.signalbuddyfinal.domain.recentpath.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum RecentPathErrorCode implements ErrorCode {

    NOT_FOUND_RECENT_PATH(HttpStatus.NOT_FOUND, "10000", "해당 경로가 존재하지 않습니다."),
    INVALID_COORDINATES(HttpStatus.BAD_REQUEST, "10001", "위도 또는 경도 값이 유효하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
