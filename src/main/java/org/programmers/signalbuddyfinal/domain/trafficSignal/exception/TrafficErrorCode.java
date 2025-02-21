package org.programmers.signalbuddyfinal.domain.trafficSignal.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TrafficErrorCode implements ErrorCode {
    TRAFFIC_SIGNAL_API_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "03500", "보행등 API 요청이 실패했습니다."),
    ALREADY_EXIST_TRAFFIC_SIGNAL(HttpStatus.CONFLICT, "03501", "이미 존재하는 보행등입니다.");

    private HttpStatus httpStatus;
    private String code;
    private String message;
}
