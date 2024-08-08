package com.home.quartzapp.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    // Scheduler
    SCHE0001("Job already exist.", HttpStatus.BAD_REQUEST),
    SCHE0002("Job does not exist.", HttpStatus.NOT_FOUND),
    SCHE0003("Job class not found.", HttpStatus.BAD_REQUEST),
    SCHE0004("Scheduler exception error.", HttpStatus.INTERNAL_SERVER_ERROR),
    SCHE0005("It is not an interruptible job.", HttpStatus.BAD_REQUEST),
    SCHE0006("A job cannot be created as it is not a Quartz job class.", HttpStatus.BAD_REQUEST),
    SCHE0007("Undefined Job command..", HttpStatus.BAD_REQUEST),

    // Security
    SCR0001("The login Id or password is incorrect.", HttpStatus.UNAUTHORIZED),
    SCR0002("Credentials have expired.", HttpStatus.UNAUTHORIZED),
    SCR0003("Rejected because the credentials are not configured properly.", HttpStatus.UNAUTHORIZED),
    SCR0004("Refresh token have expired.", HttpStatus.UNAUTHORIZED),
    SCR0005("Refresh token invalid.", HttpStatus.UNAUTHORIZED),
    SCR0006("User authentication is invalid.", HttpStatus.UNAUTHORIZED),


    // Common Error
    CMNE0001("Internal server error. %s", HttpStatus.INTERNAL_SERVER_ERROR),
    CMNE0002("Request not valid.", HttpStatus.BAD_REQUEST),
    CMNE0003("Method not allowed.", HttpStatus.METHOD_NOT_ALLOWED),
    CMNE0004("Invalid parameter.", HttpStatus.BAD_REQUEST),
    CMNE0005("Unauthorized Request.", HttpStatus.UNAUTHORIZED),
    CMNE0006("Method not allowed.", HttpStatus.METHOD_NOT_ALLOWED),
    CMNE0007("Invalid parameter.", HttpStatus.BAD_REQUEST),
    CMNE0008("Access Denied.", HttpStatus.FORBIDDEN),
    CMNE9999("Unknown error (%s).", HttpStatus.INTERNAL_SERVER_ERROR);

//    JOB_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "ERR0001", "Job already exist"),
//    JOB_DOES_NOT_EXIST(HttpStatus.BAD_REQUEST, "ERR0002", "Job does not exist"),
//    JOB_CLASS_NOT_FOUND(HttpStatus.BAD_REQUEST, "ERR0003", "Job class not found"),
//    API_REQUEST_NOT_VALID(HttpStatus.BAD_REQUEST, "ERR0005", "Request not valid"),
//    HTTP_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "ERR0006", "Method not allowed"),
//    HTTP_INVALID_PARAM(HttpStatus.BAD_REQUEST, "ERR0007", "Invalid parameter"),
//    HTTP_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "ERR0008", "Unauthorized Request"),
//    HTTP_FORBIDDEN(HttpStatus.FORBIDDEN, "ERR0009", "Forbidden"),
//
//    JOB_SCHEDULER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "ERR9001", "Scheduler exception error"),
//
//    HTTP_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ERR9002", "Internal server error");
    private final String errorMessageFormat;
    private final HttpStatus httpStatus;
}
