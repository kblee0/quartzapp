package com.home.quartzapp.common.exception;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    //모든 예외를 ApiError 형식으로 반환한다.
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleException(Throwable  e) {
        String cause = null;

        ApiException apiException = switch (e) {
            case ApiException ignore -> (ApiException)e;
            case MethodArgumentNotValidException t -> { cause = t.getBody().toString();  yield  ApiException.code("CNME0002"); }
            case ConstraintViolationException ignore -> ApiException.code("CMNE0002");
            case HttpRequestMethodNotSupportedException ignore -> ApiException.code("CMNE0006");
            //@Valid 검증 실패 시 Catch
            case IllegalArgumentException ignored -> ApiException.code("CMNE0007");
            //Role Check 오류
            case AuthorizationDeniedException t -> { cause = t.getAuthorizationResult().toString(); yield  ApiException.code("CMNE0008"); }
            default -> ApiException.code("CMNE0001", e.getMessage());
        };
        log.error(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        log.error(">> ErrorCode: {}", apiException.getErrorCode());
        log.error(">> ErrorMessage: {}", apiException.getErrorMessage());
        if (cause != null) {
            log.error(">> Cause: {}", cause);
        }
        StringBuffer stackTrace = new StringBuffer();
        Arrays.stream(e.getStackTrace()).limit(10).forEach(m -> stackTrace.append("\n\t").append(m));
        if(!stackTrace.isEmpty()) stackTrace.append("\n\t...");
        log.error(">> Exception: {}{}", e.getClass(), stackTrace);
        log.error(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        return ResponseEntity.status(apiException.getHttpStatus()).body(apiException.body());
    }
}