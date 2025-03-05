package org.programmers.signalbuddyfinal.domain.notification.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FcmErrorCode implements ErrorCode {

    FCM_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "19000", "FCM에 메시지 전송이 실패했습니다."),
    FCM_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "19001", "해당 FCM Token을 찾지 못했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
