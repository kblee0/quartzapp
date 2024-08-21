package com.home.quartzapp.common.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;

@Getter
@Slf4j
public class ErrorCodeException extends RuntimeException {

    private String errorCode;

    protected String COMMON_MISCELLANEOUS_ERROR = "CMNE9999";

    public ErrorCodeException(String errorCode) {
        super(ErrorCode.valueOf(errorCode).getErrorMessageFormat());
        this.errorCode = errorCode;
    }
    public ErrorCodeException(String errorCode, Object arg) {
        super(MessageFormatter.basicArrayFormat(ErrorCode.valueOf(errorCode).getErrorMessageFormat(), new Object[] { arg }));
        this.errorCode = errorCode;
    }
    public ErrorCodeException(String errorCode, Object ...args) {
        super(MessageFormatter.basicArrayFormat(ErrorCode.valueOf(errorCode).getErrorMessageFormat(), args));
        this.errorCode = errorCode;
    }

    public ErrorCodeException(String errorCode, Throwable cause) {
        super(ErrorCode.valueOf(errorCode).getErrorMessageFormat(), cause);
        this.errorCode = errorCode;
    }
    public ErrorCodeException(String errorCode, Throwable cause, Object ...args) {
        super(MessageFormatter.basicArrayFormat(ErrorCode.valueOf(errorCode).getErrorMessageFormat(), args), cause);
        this.errorCode = errorCode;
    }
}
