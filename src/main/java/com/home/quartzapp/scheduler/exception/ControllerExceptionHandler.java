package com.home.quartzapp.scheduler.exception;

import java.security.InvalidParameterException;
import java.text.MessageFormat;

import jakarta.validation.ConstraintViolationException;

import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.home.quartzapp.scheduler.dto.ApiErrorDto;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

    @ExceptionHandler(ApiException.class)
    protected ResponseEntity<?> handleApiException(ApiException e) {
        log.error("handleApiException", e);

        return ApiErrorDto.create(e.getError());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentNotValidException", e);

        FieldError fieldError = e.getBindingResult().getFieldError();
        if( fieldError != null) {
            String cause = MessageFormat.format("{0}.{1}: {2}",
                    fieldError.getObjectName(),
                    fieldError.getField(),
                    fieldError.getDefaultMessage());
            return ApiErrorDto.create(ErrorCode.API_REQUEST_NOT_VALID, cause);
        }
        else {
            return ApiErrorDto.create(ErrorCode.API_REQUEST_NOT_VALID);
        }
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("handleConstraintViolationException", e);

        return ApiErrorDto.create(ErrorCode.API_REQUEST_NOT_VALID, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("handleHttpRequestMethodNotSupportedException", e);

        return ApiErrorDto.create(ErrorCode.HTTP_METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("handleIllegalArgumentException", e);

        return ApiErrorDto.create(ErrorCode.HTTP_INVALID_PARAM, e.getMessage());
    }

    //@Valid 검증 실패 시 Catch
    @ExceptionHandler(InvalidParameterException.class)
    protected ResponseEntity<?> handleInvalidParameterException(InvalidParameterException e) {
        log.error("handleInvalidParameterException", e);

        return ApiErrorDto.create(ErrorCode.HTTP_INVALID_PARAM, e.getMessage());
    }

    @ExceptionHandler(SchedulerException.class)
    protected ResponseEntity<?> handleSchedulerException(SchedulerException e) {
        log.error("handleSchedulerException", e);

        return ApiErrorDto.create(ErrorCode.HTTP_INVALID_PARAM, e.getMessage());
    }


    //모든 예외를 ApiError 형식으로 반환한다.
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleException(Exception e) {
        log.error("handleException", e);

        return ApiErrorDto.create(ErrorCode.HTTP_INTERNAL_SERVER_ERROR, e.getMessage());
    }
}