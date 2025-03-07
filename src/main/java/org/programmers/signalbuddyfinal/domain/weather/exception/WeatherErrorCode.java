package org.programmers.signalbuddyfinal.domain.weather.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum WeatherErrorCode implements ErrorCode {

    EXCEL_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "18000", "엑셀 데이터 읽는 중 오류 발생"),
    WEATHER_API_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "18001","날씨 API 요청이 실패했습니다."),
    WEATHER_API_RESPONSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "18002", "날씨 API 데이터 파싱 에러"),
    RESPONSE_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"18003", "WEATHER -> WEATHER_RESPONSE 변환 에러"),
    WEATHER_API_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"18004" , "기상청 OpenAPI SERVICE ERROR"),
    WEATHER_SECURITY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"18005" , "허용되지 않은 경로 접근 시도");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
