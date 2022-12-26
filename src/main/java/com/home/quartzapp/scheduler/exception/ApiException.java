package com.home.quartzapp.scheduler.exception;

import com.home.quartzapp.scheduler.dto.ApiError;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private ApiError error;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.error = new ApiError(errorCode);
    }

    public ApiException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getErrorMessage(), cause);
        this.error = new ApiError(errorCode, cause);
    }

    public ApiException(ErrorCode errorCode, String cause) {
        super(errorCode.getErrorMessage());
        this.error = new ApiError(errorCode, cause);
    }

}