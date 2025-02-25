package org.programmers.signalbuddyfinal.domain.postit.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.domain.feedback.exception.FeedbackErrorCode;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PostItErrorCode implements ErrorCode {

    NOT_FOUND_POSTIT(HttpStatus.NOT_FOUND, "11000", "해당 포스트잇을 찾을 수 없습니다."),
    POSTIT_MODIFIER_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "11001", "해당 포스트잇 작성자와 사용자가 다릅니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
