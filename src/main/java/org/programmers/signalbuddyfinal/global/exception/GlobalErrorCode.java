package org.programmers.signalbuddyfinal.global.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GlobalErrorCode implements ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "09000", "잘못된 요청입니다."),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "09001", "알 수 없는 에러가 발생했습니다. 관리자에게 문의하세요."),
    ADMIN_ONLY(HttpStatus.FORBIDDEN, "09002", "접근할 권한이 없습니다."),
    FILE_LOAD_ERROR_NOT_EXIST_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "09003", "유효하지 않은 URL 또는 읽을 수 없는 파일입니다."),
    FILE_UPLOAD_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "09004", "파일 저장 중 오류가 발생했습니다."),
    FILE_LOAD_ERROR_INVALID_URL(HttpStatus.INTERNAL_SERVER_ERROR, "09005", "URL 생성 중 오류가 발생했습니다."),
    S3_UPLOAD_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "09006", "S3 업로드 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
