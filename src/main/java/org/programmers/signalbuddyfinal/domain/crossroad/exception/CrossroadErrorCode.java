package org.programmers.signalbuddyfinal.domain.crossroad.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CrossroadErrorCode implements ErrorCode {

    CROSSROAD_API_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "03000", "교차로 API 요청이 실패했습니다."),
    ALREADY_EXIST_CROSSROAD(HttpStatus.CONFLICT, "03001", "이미 존재하는 교차로입니다."),
    NOT_FOUND_CROSSROAD(HttpStatus.NOT_FOUND, "03002", "존재하지 않는 교차로입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
