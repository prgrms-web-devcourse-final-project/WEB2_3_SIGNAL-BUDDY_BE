package org.programmers.signalbuddyfinal.domain.admin.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AdminErrorCode implements ErrorCode {

    END_DATE_NOT_SELECTED(HttpStatus.BAD_REQUEST, 00000, "종료일이 선택되지 않았습니다."),
    START_DATE_NOT_SELECTED(HttpStatus.BAD_REQUEST, 00001, "시작일이 선택되지 않았습니다."),
    DUPLICATED_DATE(HttpStatus.BAD_REQUEST, 00002, "조회 기간이 중복 선택되었습니다.");

    private HttpStatus httpStatus;
    private int code;
    private String message;
}
