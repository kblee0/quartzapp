package com.home.quartzapp.common.exception;

import java.security.InvalidParameterException;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    protected ResponseEntity<?> handleApiException(ApiException e) {
        log.error("handleApiException", e);

        return ResponseEntity.status(e.getHttpStatus()).body(e.body());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentNotValidException", e);

        ApiException apiException = new ApiException("CMNE0002");

        return ResponseEntity.status(apiException.getHttpStatus()).body(apiException.body());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("handleConstraintViolationException", e);

        ApiException apiException = new ApiException("CMNE0002");

        return ResponseEntity.status(apiException.getHttpStatus()).body(apiException.body());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("handleHttpRequestMethodNotSupportedException", e);

        ApiException apiException = new ApiException("CMNE0006");

        return ResponseEntity.status(apiException.getHttpStatus()).body(apiException.body());
    }

    //@Valid 검증 실패 시 Catch
    @ExceptionHandler({IllegalArgumentException.class,InvalidParameterException.class})
    protected ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("handleIllegalArgumentException", e);

        ApiException apiException = new ApiException("CMNE0007");

        return ResponseEntity.status(apiException.getHttpStatus()).body(apiException.body());
    }

    //모든 예외를 ApiError 형식으로 반환한다.
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleException(Exception e) {
        log.error("handleException", e);

        ApiException apiException = new ApiException("CMNE0001", e.getMessage());

        return ResponseEntity.status(apiException.getHttpStatus()).body(apiException.body());
    }
}