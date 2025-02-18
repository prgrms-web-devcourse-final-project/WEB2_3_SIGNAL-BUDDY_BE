package org.programmers.signalbuddyfinal.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.global.constant.ResponseStatus;

/**
 * API 응답의 표준 형식을 정의하는 DTO
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {

    private String status;
    private String message;
    /**
     * 응답 데이터 (null일 경우 JSON에 포함되지 않음)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    /**
     * 빌더를 이용한 생성자.
     *
     * @param status  응답 상태
     * @param message 응답 메시지
     * @param data    응답 데이터
     */
    @Builder
    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * 데이터 포함 성공 응답을 생성합니다.
     *
     * @param data 성공 응답 데이터
     * @param <T>  데이터 타입
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> createSuccess(T data) {
        return ApiResponse.<T>builder().status(ResponseStatus.SUCCESS.getMsg()).message(null)
            .data(data).build();
    }

    /**
     * 데이터 없는 성공 응답을 생성합니다.
     *
     * @return 성공 응답 객체
     */
    public static ApiResponse<Object> createSuccessWithNoData() {
        return ApiResponse.builder().status(ResponseStatus.SUCCESS.getMsg()).message(null)
            .data(null).build();
    }

    /**
     * 에러 응답을 생성합니다.
     *
     * @param msg 에러 메시지
     * @return 에러 응답 객체
     */
    public static ApiResponse<Object> createError(String msg) {
        return ApiResponse.builder().status(ResponseStatus.ERROR.getMsg()).message(msg).data(null)
            .build();
    }
}