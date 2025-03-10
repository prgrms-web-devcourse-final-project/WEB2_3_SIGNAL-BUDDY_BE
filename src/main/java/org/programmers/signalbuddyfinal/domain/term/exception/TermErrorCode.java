package org.programmers.signalbuddyfinal.domain.term.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TermErrorCode implements ErrorCode {

    ALREADY_EXIST_TERM(HttpStatus.CONFLICT, "15000", "해당 카테고리에 이미 같은 버전의 약관이 존재합니다."),
    EFFECTIVE_DATE_CHANGE_REQUIRED(HttpStatus.CONFLICT, "15001", "이전 약관의 유효 기간과 겹칩니다."),
    START_DATE_AFTER_END_DATE(HttpStatus.BAD_REQUEST,"15002", "시작일은 종료일보다 이전이어야 합니다."),
    NO_EXIST_TERM_NOW(HttpStatus.NOT_FOUND, "15003", "현재 시행되고 있는 약관이 존재하지 않습니다."),
    NO_EXIST_TERM(HttpStatus.NOT_FOUND, "15004", "해당 약관이 존재하지 않습니다.");

    private HttpStatus httpStatus;
    private String code;
    private String message;
}
