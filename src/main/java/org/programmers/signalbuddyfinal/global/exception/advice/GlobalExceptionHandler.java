package org.programmers.signalbuddyfinal.global.exception.advice;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.programmers.signalbuddyfinal.global.exception.GlobalErrorCode;
import org.programmers.signalbuddyfinal.global.exception.advice.dto.ErrorResponse;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        logError(e);
        ErrorCode error = e.getErrorCode();
        return ResponseEntity.status(error.getHttpStatus()).body(new ErrorResponse(error));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
        MethodArgumentTypeMismatchException e
    ) {
        logError(e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                    GlobalErrorCode.BAD_REQUEST.getCode(), "잘못된 형식으로 요청했습니다."
            ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidException(MethodArgumentNotValidException e) {
        logError(e);
        String code = GlobalErrorCode.BAD_REQUEST.getCode();
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        ErrorResponse errorResponse = new ErrorResponse(code, message);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidException(ConstraintViolationException e) {
        logError(e);
        String code = GlobalErrorCode.BAD_REQUEST.getCode();
        String message = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(code, message);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDataAccessApiUsage(
        InvalidDataAccessApiUsageException e
    ) {
        logError(e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(GlobalErrorCode.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        logError(e);
        ErrorResponse errorResponse = new ErrorResponse(GlobalErrorCode.SERVER_ERROR);
        return ResponseEntity.internalServerError().body(errorResponse);
    }

    private void logError(Exception e) {
        log.error("Exception occurred: [{}] - {}", e.getClass().getSimpleName(), e.getMessage());
    }
}
