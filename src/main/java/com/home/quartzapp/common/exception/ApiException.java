package com.home.quartzapp.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.quartzapp.common.util.ApplicationContextProvider;
import com.home.quartzapp.common.util.ExceptionUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

@Getter
@Slf4j
public class ApiException extends ErrorCodeException {
    private HttpStatus httpStatus;

    public ApiException(String errorCode) {
        super(errorCode);
        this.httpStatus = ErrorCode.valueOf(this.getErrorCode()).getHttpStatus();
    }
    public ApiException(String errorCode, Object arg) {
        super(errorCode, arg);
        this.httpStatus = ErrorCode.valueOf(this.getErrorCode()).getHttpStatus();
    }

    public ApiException(String errorCode, Object ...args) {
        super(errorCode, args);
        this.httpStatus = ErrorCode.valueOf(this.getErrorCode()).getHttpStatus();
    }

    public ApiException(String errorCode, Throwable cause, Object ...args) {
        super(errorCode, cause, args);
        this.httpStatus = ErrorCode.valueOf(this.getErrorCode()).getHttpStatus();
    }

    public static ApiException code(String errorCode) {
        return new ApiException(errorCode);
    }
    public static ApiException code(String errorCode, Object arg) { return new ApiException(errorCode, arg); }
    public static ApiException code(String errorCode, Object arg1, Object arg2) { return new ApiException(errorCode, arg1, arg2); }
    public static ApiException code(String errorCode, Object ...args) {
        return new ApiException(errorCode, args);
    }
    public static  ApiException code(String errorCode, Throwable cause, Object ...args) { return new ApiException(errorCode, cause, args); }

    public ApiException setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public ApiException log() {
        ExceptionUtil.log(this);
        return this;
    }

    public ApiErrorDto body() {
        return new ApiErrorDto(this.getErrorCode(), this.getMessage());
    }

    public void responseWrite(HttpServletResponse response) {
        ObjectMapper objectMapper = ApplicationContextProvider.getBean("objectMapper", ObjectMapper.class);

        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().write(objectMapper.writeValueAsString(body()));
        } catch (IOException e) {
            log.error("ApiException response IOException error :: {}", body());
            log.error("IOException :: {}", e.getMessage());
            throw ApiException.code("CMNE0001", e, e.getMessage());
        }
    }
}
