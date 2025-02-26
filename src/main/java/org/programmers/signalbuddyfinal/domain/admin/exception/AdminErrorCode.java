package org.programmers.signalbuddyfinal.domain.admin.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AdminErrorCode implements ErrorCode {

    END_DATE_NOT_SELECTED(HttpStatus.BAD_REQUEST, "00001", "종료일이 선택되지 않았습니다."),
    START_DATE_NOT_SELECTED(HttpStatus.BAD_REQUEST, "00002", "시작일이 선택되지 않았습니다."),
    DUPLICATED_PERIOD(HttpStatus.BAD_REQUEST, "00003", "조회 기간이 중복 선택되었습니다."),
    START_DATE_AFTER_END_DATE(HttpStatus.BAD_REQUEST, "00004", "시작일이 종료일보다 미래입니다.");

    private HttpStatus httpStatus;
    private String code;
    private String message;
}
