package org.programmers.signalbuddyfinal.domain.like.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LikeErrorCode implements ErrorCode {

    ALREADY_ADDED_LIKE(HttpStatus.CONFLICT, "05000", "이미 좋아요 추가되었습니다."),
    NOT_FOUND_LIKE(HttpStatus.NOT_FOUND, "05001", "좋아요 데이터가 존재하지 않습니다."),
    Illegal_REQUEST_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "05002", "잘못된 좋아요 요청 상태입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
