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
public class ApiErrorDto {
    private String errorCode;
    private String errorMessage;
    private String cause;
    private final LocalDateTime timestamp = LocalDateTime.now();
    
    @JsonIgnore
    private HttpStatus status;

    public ApiErrorDto(ErrorCode errorCode) {
        this.errorCode = errorCode.getErrorCode();
        this.errorMessage = errorCode.getErrorMessage();
        this.status = errorCode.getStatus();
    }

    public ApiErrorDto(ErrorCode errorCode, Throwable cause) {
        this.errorCode = errorCode.getErrorCode();
        this.errorMessage = errorCode.getErrorMessage();
        this.cause = cause.getMessage();
        this.status = errorCode.getStatus();
    }

    public ApiErrorDto(ErrorCode errorCode, String cause) {
        this.errorCode = errorCode.getErrorCode();
        this.errorMessage = errorCode.getErrorMessage();
        this.cause = cause;
        this.status = errorCode.getStatus();
    }

    public static ResponseEntity<?> create(ApiErrorDto apiErrorDto) {
        return new ResponseEntity<>(apiErrorDto, apiErrorDto.getStatus());
    }

    public static ResponseEntity<?> create(ErrorCode error) {
        ApiErrorDto apiErrorDto = new ApiErrorDto(error);

        return new ResponseEntity<>(apiErrorDto, error.getStatus());
    }

    public static ResponseEntity<?> create(ErrorCode error, String cause) {
        ApiErrorDto apiErrorDto = new ApiErrorDto(error, cause);
        
        return new ResponseEntity<>(apiErrorDto, error.getStatus());
    }
}
