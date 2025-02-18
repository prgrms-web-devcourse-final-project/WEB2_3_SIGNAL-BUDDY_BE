package org.programmers.signalbuddyfinal.global.constant;

import lombok.Getter;

/**
 * 데이터 Response Status 속성 구성 Enum
 */
@Getter
public enum ResponseStatus {
    SUCCESS("성공"),
    ERROR("오류");

    private final String msg;

    ResponseStatus(String msg) {
        this.msg= msg;
    }
}