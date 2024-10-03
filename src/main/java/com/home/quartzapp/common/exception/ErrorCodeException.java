package com.home.quartzapp.common.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;

import java.util.Arrays;

@Getter
@Slf4j
public class ErrorCodeException extends RuntimeException {

    private final String errorCode;

    protected String COMMON_MISCELLANEOUS_ERROR = "CMNE9999";

    @Getter
    public enum Level {
        FATAL(100, "F"),
        ERROR(200, "E"),
        WARN(300, "W"),
        INFO(400, "I");

        private final int intLevel;
        private final String charLevel;
        Level(int intLevel, String charLevel) {
            this.intLevel = intLevel;
            this.charLevel = charLevel;
        }
    }

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

    public ErrorCodeException(String errorCode, Exception cause) {
        super(ErrorCode.valueOf(errorCode).getErrorMessageFormat(), cause);
        this.errorCode = errorCode;
    }
    public ErrorCodeException(String errorCode, Exception cause, Object ...args) {
        super(MessageFormatter.basicArrayFormat(ErrorCode.valueOf(errorCode).getErrorMessageFormat(), args), cause);
        this.errorCode = errorCode;
    }

    public Level getLevel() {
        return Arrays.stream(Level.values()).filter(v -> v.getCharLevel().equals(errorCode.substring(3,4))).findFirst().orElse(Level.ERROR);
    }
}
