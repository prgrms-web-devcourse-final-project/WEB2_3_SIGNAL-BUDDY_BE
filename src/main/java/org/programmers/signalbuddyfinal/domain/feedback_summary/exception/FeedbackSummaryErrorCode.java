package org.programmers.signalbuddyfinal.domain.feedback_summary.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FeedbackSummaryErrorCode implements ErrorCode {

    NOT_FOUND_FEEDBACK_SUMMARY(HttpStatus.NOT_FOUND, "16000", "해당 피드백 통계를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
