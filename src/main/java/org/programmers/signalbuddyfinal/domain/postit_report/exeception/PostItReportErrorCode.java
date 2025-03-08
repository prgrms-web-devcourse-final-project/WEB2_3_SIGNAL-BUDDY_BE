package org.programmers.signalbuddyfinal.domain.postit_report.exeception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PostItReportErrorCode implements ErrorCode {

    NOT_FOUND_POSTIT_REPORT(HttpStatus.NOT_FOUND, "18000", "해당 포스트잇 신고를 찾을 수 없습니다."),
    POSTIT_MODIFIER_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "18001", "해당 포스트잇 신고자와 사용자가 다릅니다."),
    ALREADY_REPORTED_POSITI(HttpStatus.FORBIDDEN, "18002", "이미 신고한 포스트잇 입니다."),
    NOT_FOUND_REPORTED_POSITI(HttpStatus.FORBIDDEN, "18003", "신고한 적 없는 포스트잇 입니다."),
    ALREADY_SUSPENDED_POSTIT(HttpStatus.NOT_FOUND, "18004", "게시 중지된 포스트잇 입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
