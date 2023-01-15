package com.home.quartzapp.scheduler.exception;

import com.home.quartzapp.scheduler.dto.ApiErrorDto;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private ApiErrorDto error;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.error = new ApiErrorDto(errorCode);
    }

    public ApiException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getErrorMessage(), cause);
        this.error = new ApiErrorDto(errorCode, cause);
    }

    public ApiException(ErrorCode errorCode, String cause) {
        super(errorCode.getErrorMessage());
        this.error = new ApiErrorDto(errorCode, cause);
    }

}