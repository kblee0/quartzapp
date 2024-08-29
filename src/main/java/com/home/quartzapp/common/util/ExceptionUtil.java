package com.home.quartzapp.common.util;

import com.home.quartzapp.common.exception.ErrorCodeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;

@Slf4j
public class ExceptionUtil {
    public static String getStackTrace(Exception e) {
        if(e == null) return null;

        StringBuffer stackTrace = new StringBuffer(e.getClass().getName());
        Arrays.stream(e.getStackTrace()).limit(15).forEach(m -> stackTrace.append("\n\t").append(m));
        if(!stackTrace.isEmpty() && e.getStackTrace().length > 15) stackTrace.append("\n\t...");

        return stackTrace.toString();
    }

    public static String getDetailMessage(Exception e) {
        if(e == null) return null;

        //@Valid 검증 실패 시 Catch
        //Role Check 오류
        return switch (e) {
            case MethodArgumentNotValidException t -> t.getBody().toString();
            //Role Check 오류
            case AuthorizationDeniedException t -> t.getAuthorizationResult().toString();
            default -> e.getMessage();
        };
    }

    public static ErrorCodeException findErrorCodeException(Exception exception) {
        for(Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if(cause instanceof ErrorCodeException) {
                return (ErrorCodeException) cause;
            }
        }
        return null;
    }

    public static void log(Exception exception) {
        ErrorCodeException errorCodeException = findErrorCodeException(exception);

        if(errorCodeException != null) {
            log.error(">> An ErrorCodeException was thrown :: ErrorCode: {}, ErrorMessage: {}",
                    errorCodeException.getErrorCode(),
                    errorCodeException.getMessage(),
                    exception);
        }
        else {
            log.error(">> An Exception was thrown :: {}", exception.getMessage(), exception);
        }
    }
}
