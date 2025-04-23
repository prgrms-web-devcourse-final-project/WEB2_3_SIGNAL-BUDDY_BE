package org.programmers.signalbuddyfinal.domain.air_quality.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AirQualityErrorCode implements ErrorCode {

    AIR_QUALITY_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "20000",
        "일시적인 문제로 데이터를 불러올 수 없습니다. 잠시 후 다시 시도해 주세요."),
    AIR_QUALITY_DATA_NOT_READ(HttpStatus.INTERNAL_SERVER_ERROR, "20001", "미세먼지 API 데이터 파싱 에러");

    private HttpStatus httpStatus;
    private String code;
    private String message;
}
