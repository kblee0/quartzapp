package com.home.quartzapp.common.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ApiException extends RuntimeException {
    private String errorCode;
    private String errorMessage;
    private HttpStatus httpStatus;

    public ApiException(String errorCode, Object ...args) {
        this.setErrorCode(errorCode);
        try {
            this.setHttpStatus(ErrorCode.valueOf(errorCode).getHttpStatus());
            this.setErrorMessage(String.format(ErrorCode.valueOf(errorCode).getErrorMessageFormat(), args));
        } catch (IllegalArgumentException e) {
            this.setHttpStatus(ErrorCode.valueOf("CMNE9999").getHttpStatus());
            this.setErrorMessage(String.format(ErrorCode.valueOf("CMNE9999").getErrorMessageFormat(), errorCode));
        }
    }
    public ApiException(String errorCode, HttpStatus httpStatus, Object ...args) {
        this.setErrorCode(errorCode);
        try {
            this.setHttpStatus(httpStatus);
            this.setErrorMessage(String.format(ErrorCode.valueOf(errorCode).getErrorMessageFormat(), args));
        } catch (IllegalArgumentException e) {
            this.setHttpStatus(httpStatus);
            this.setErrorMessage(String.format(ErrorCode.valueOf("CMNE9999").getErrorMessageFormat(), errorCode));
        }
    }

    public ApiErrorDto body() {
        return new ApiErrorDto(this.getErrorCode(), this.getErrorMessage());
    }
    public static ApiException code(String errorCode, Object ...args) {
        return new ApiException(errorCode, args);
    }
    public static ApiException noContent(String errorCode, Object ...args) {
        return new ApiException(errorCode, HttpStatus.NO_CONTENT, args);
    }
    public static ApiException badRequest(String errorCode, Object ...args) {
        return new ApiException(errorCode, HttpStatus.BAD_REQUEST, args);
    }
    public static ApiException notFound(String errorCode, Object ...args) {
        return new ApiException(errorCode, HttpStatus.NOT_FOUND, args);
    }
    public static ApiException internalServerError(String errorCode, Object ...args) {
        return new ApiException(errorCode, HttpStatus.INTERNAL_SERVER_ERROR, args);
    }
}
