package com.home.quartzapp.common.util;

import jakarta.validation.ConstraintViolationException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;

public class ExceptionUtil {
    public static String getStackTrace(Exception e) {
        if(e == null) return null;

        StringBuffer stackTrace = new StringBuffer(e.getClass().getName());
        Arrays.stream(e.getStackTrace()).limit(15).forEach(m -> stackTrace.append("\n\t").append(m));
        if(!stackTrace.isEmpty() && e.getStackTrace().length > 15) stackTrace.append("\n\t...");

        return stackTrace.toString();
    }

    public static String getCause(Exception e) {
        if(e == null) return null;

        String cause = switch (e) {
            case MethodArgumentNotValidException t -> t.getBody().toString();
            case ConstraintViolationException ignore -> null;
            case HttpRequestMethodNotSupportedException ignore -> null;
            //@Valid 검증 실패 시 Catch
            case IllegalArgumentException ignore -> null;
            //Role Check 오류
            case AuthorizationDeniedException t -> t.getAuthorizationResult().toString();
            default -> null;
        };
        return cause;
    }
}
