package com.home.quartzapp.scheduler.dto;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.home.quartzapp.scheduler.exception.ErrorCode;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class ApiError {
    private String errorCode;
    private String errorMessage;
    private String cause;
    private final LocalDateTime timestamp = LocalDateTime.now();
    
    @JsonIgnore
    private HttpStatus status;

    public ApiError(ErrorCode errorCode) {
        this.errorCode = errorCode.getErrorCode();
        this.errorMessage = errorCode.getErrorMessage();
        this.status = errorCode.getStatus();
    }

    public ApiError(ErrorCode errorCode, Throwable cause) {
        this.errorCode = errorCode.getErrorCode();
        this.errorMessage = errorCode.getErrorMessage();
        this.cause = cause.getMessage();
        this.status = errorCode.getStatus();
    }

    public ApiError(ErrorCode errorCode, String cause) {
        this.errorCode = errorCode.getErrorCode();
        this.errorMessage = errorCode.getErrorMessage();
        this.cause = cause;
        this.status = errorCode.getStatus();
    }

    public static ResponseEntity<?> create(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    public static ResponseEntity<?> create(ErrorCode error) {
        ApiError apiError = new ApiError(error);

        return new ResponseEntity<>(apiError, error.getStatus());
    }

    public static ResponseEntity<?> create(ErrorCode error, String cause) {
        ApiError apiError = new ApiError(error, cause);
        
        return new ResponseEntity<>(apiError, error.getStatus());
    }
}
