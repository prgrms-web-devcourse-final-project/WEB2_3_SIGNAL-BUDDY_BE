package org.programmers.signalbuddyfinal.domain.feedback_report.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FeedbackReportErrorCode implements ErrorCode {

    NOT_FOUND_FEEDBACK_REPORT(HttpStatus.NOT_FOUND, "15000", "해당 피드백 신고를 찾을 수 없습니다."),
    ELIMINATOR_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "15001", "삭제 요청자와 해당 피드백 신고의 작성자가 일치하지 않습니다."),
    CATEGORY_BAD_REQUEST(HttpStatus.BAD_REQUEST, "15002", "잘못된 피드백 신고 유형입니다."),
    FEEDBACK_REPORT_MISMATCH(HttpStatus.BAD_REQUEST, "15003", "해당 신고와 피드백이 관련이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
