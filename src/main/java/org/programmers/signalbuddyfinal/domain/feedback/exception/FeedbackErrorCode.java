package org.programmers.signalbuddyfinal.domain.feedback.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FeedbackErrorCode implements ErrorCode {

    NOT_FOUND_FEEDBACK(HttpStatus.NOT_FOUND, "04000", "해당 피드백을 찾을 수 없습니다."),
    FEEDBACK_MODIFIER_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "04001", "해당 게시물의 작성자와 일치하지 않습니다."),
    FEEDBACK_ELIMINATOR_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "04002", "해당 게시물의 작성자와 일치하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
