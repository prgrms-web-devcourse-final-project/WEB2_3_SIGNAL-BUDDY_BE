package org.programmers.signalbuddyfinal.domain.trafficSignal.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TrafficErrorCode implements ErrorCode {
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND , "03500", "파일을 찾을 수 없습니다."),
    ALREADY_EXIST_TRAFFIC_SIGNAL(HttpStatus.CONFLICT, "03501", "이미 존재하는 보행등입니다."),
    NOT_FOUND_TRAFFIC(HttpStatus.NOT_FOUND, "03502","해당 보행등이 존재하지 않습니다.");

    private HttpStatus httpStatus;
    private String code;
    private String message;
}
