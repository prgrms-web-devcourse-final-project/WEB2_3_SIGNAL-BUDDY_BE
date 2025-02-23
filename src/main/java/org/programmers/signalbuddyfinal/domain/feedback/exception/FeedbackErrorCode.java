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
    FEEDBACK_MODIFIER_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "04001", "수정 요청자와 해당 피드백의 작성자가 일치하지 않습니다."),
    FEEDBACK_ELIMINATOR_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "04002", "삭제 요청자와 해당 피드백의 작성자가 일치하지 않습니다."),
    FEEDBACK_CATEGORY_BAD_REQUEST(HttpStatus.BAD_REQUEST, "04003", "잘못된 피드백 유형입니다."),
    SECRET_FEEDBACK_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "04004", "해당 피드백에 접근할 권한이 없습니다."),
    ANSWER_STATUS_BAD_REQUEST(HttpStatus.BAD_REQUEST, "04005", "잘못된 피드백 답변 상태입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
