package org.programmers.signalbuddyfinal.domain.bookmark.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum BookmarkErrorCode implements ErrorCode {

    NOT_FOUND_BOOKMARK(HttpStatus.NOT_FOUND, "01000", "해당 즐겨찾기를 찾을 수 없습니다."),
    INVALID_COORDINATES(HttpStatus.BAD_REQUEST, "01001", "위도 또는 경도 값이 유효하지 않습니다."),
    UNAUTHORIZED_MEMBER_ACCESS(HttpStatus.FORBIDDEN, "01002", "해당 즐겨찾기를 접근할 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
