package com.home.quartzapp.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ErrorCode {
    // Scheduler
    SCHE0001("Job already exist.", HttpStatus.BAD_REQUEST),
    SCHE0002("Job does not exist.", HttpStatus.NOT_FOUND),
    SCHE0003("Job class not found.", HttpStatus.BAD_REQUEST),
    SCHE0004("Scheduler error: {}", HttpStatus.INTERNAL_SERVER_ERROR),
    SCHE0005("It is not an interruptible job.", HttpStatus.BAD_REQUEST),
    SCHE0006("A job cannot be created as it is not a Quartz job class.", HttpStatus.BAD_REQUEST),
    SCHE0007("Undefined Job command.", HttpStatus.BAD_REQUEST),

    // Security
    SCRW001("The login Id or password is incorrect.", HttpStatus.UNAUTHORIZED),
    SCRW002("Credentials have expired.", HttpStatus.UNAUTHORIZED),
    SCRW003("Rejected because the credentials are not configured properly.", HttpStatus.UNAUTHORIZED),
    SCRW004("Refresh token have expired.", HttpStatus.UNAUTHORIZED),
    SCRW005("Refresh token invalid.", HttpStatus.UNAUTHORIZED),
    SCRW006("User authentication is invalid.", HttpStatus.UNAUTHORIZED),


    // Common Error
    CMNE0001("Internal server error: {}", HttpStatus.INTERNAL_SERVER_ERROR),
    CMNE0002("Request not valid.", HttpStatus.BAD_REQUEST),
    CMNE0003("Method not allowed.", HttpStatus.METHOD_NOT_ALLOWED),
    CMNE0004("Request parse error.", HttpStatus.BAD_REQUEST),
    CMNW0005("Unauthorized Request.", HttpStatus.UNAUTHORIZED),
    CMNE0006("Method not allowed.", HttpStatus.METHOD_NOT_ALLOWED),
    CMNE0007("Invalid parameter.", HttpStatus.BAD_REQUEST),
    CMNW0008("Access Denied.", HttpStatus.FORBIDDEN),
    CNME0009("Not found.", HttpStatus.NOT_FOUND),
    CMNE9999("Miscellaneous errors: {}", HttpStatus.INTERNAL_SERVER_ERROR),

    // Quartz Job Error
    QJBE0001("Invalid jobDataMap error. (key={})", null),
    QJBE0002("The job ended abnormally.", null),
    QJBE0003("The job was processed, but there were errors in the results.", null),
    QJBE0004("TThe job was processed, but an error occurred while processing the results.", null),
    QJBE0005("Failed to create job for \"{}\".", null),
    QJBE0006("Failed to save {}}.", null),
    QJBE0007("'{}' batch job does not exist.", null),
    QJBE0008("'{}' batch job execution  failed.", null),
    QJBE9999("Job threw an unhandled exception.", null),

    DMYE9999("Dummy error", null);

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
    @Getter
    private final String errorMessageFormat;
    private final HttpStatus httpStatus;

    public HttpStatus getHttpStatus() {
        return httpStatus == null ? HttpStatus.INTERNAL_SERVER_ERROR : httpStatus;
    }
}
