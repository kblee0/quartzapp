package com.home.quartzapp.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    //모든 예외를 ApiError 형식으로 반환한다.
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleException(Throwable  e) {
        ApiException apiException = switch (e) {
            case ApiException ignore -> (ApiException)e;
            case MethodArgumentNotValidException cause -> ApiException.code("CNME0002", cause);
            case ConstraintViolationException cause -> ApiException.code("CMNE0002", cause);
            case HttpRequestMethodNotSupportedException cause -> ApiException.code("CMNE0006", cause);
            //@Valid 검증 실패 시 Catch
            case IllegalArgumentException cause -> ApiException.code("CMNE0007", cause);
            //Role Check 오류
            case AuthorizationDeniedException cause -> ApiException.code("CMNE0008", cause);
            case HttpMessageNotReadableException cause -> ApiException.code("CMNE0004", cause);
            default -> ApiException.code("CMNE0001", e, e.getMessage());
        };
        apiException.log();

        return ResponseEntity.status(apiException.getHttpStatus()).body(apiException.body());
    }
}