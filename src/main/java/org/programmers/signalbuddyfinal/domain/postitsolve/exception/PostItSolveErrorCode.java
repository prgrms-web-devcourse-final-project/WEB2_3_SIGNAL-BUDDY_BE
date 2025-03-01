package org.programmers.signalbuddyfinal.domain.postitsolve.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PostItSolveErrorCode implements ErrorCode {

    NOT_FOUND_POSTIT_SOLVE(HttpStatus.NOT_FOUND, "17000", "해당 해결된 포스트잇을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
