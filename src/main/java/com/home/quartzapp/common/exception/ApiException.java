package com.home.quartzapp.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.quartzapp.common.util.ApplicationContextProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.IOException;
import java.util.Arrays;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Getter
@Setter
@Slf4j
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

    public void responseWrite(HttpServletResponse response) {
        ObjectMapper objectMapper = ApplicationContextProvider.getBean("objectMapper", ObjectMapper.class);

        response.setStatus(httpStatus.value());
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().write(objectMapper.writeValueAsString(body()));
        } catch (IOException e) {
            log.error("ApiException response IOException error :: {}", body());
            log.error("IOException :: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public ApiException log(Exception e) {
        String cause = switch (e) {
            case MethodArgumentNotValidException t -> cause = t.getBody().toString();
            case ConstraintViolationException ignore -> null;
            case HttpRequestMethodNotSupportedException ignore -> null;
            //@Valid 검증 실패 시 Catch
            case IllegalArgumentException ignore -> null;
            //Role Check 오류
            case AuthorizationDeniedException t -> t.getAuthorizationResult().toString();
            default -> null;
        };

        log.error(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        log.error(">> ErrorCode: {}", this.getErrorCode());
        log.error(">> ErrorMessage: {}", this.getErrorMessage());
        if (cause != null) {
            log.error(">> Cause: {}", cause);
        }
        StringBuffer stackTrace = new StringBuffer();
        Arrays.stream(e.getStackTrace()).limit(15).forEach(m -> stackTrace.append("\n\t").append(m));
        if(!stackTrace.isEmpty()) stackTrace.append("\n\t...");
        log.error(">> Exception: {}{}", e.getClass(), stackTrace);
        log.error(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        return this;
    }
}
