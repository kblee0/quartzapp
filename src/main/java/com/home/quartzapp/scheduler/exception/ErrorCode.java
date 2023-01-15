package com.home.quartzapp.scheduler.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    JOB_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "ERR0001", "Job already exits"),
    JOB_DOES_NOT_EXIST(HttpStatus.BAD_REQUEST, "ERR0002", "Job does not exits"),
    JOB_CLASS_NOT_FOUND(HttpStatus.BAD_REQUEST, "ERR0003", "Job class not found"),
    API_REQUEST_NOT_VALID(HttpStatus.BAD_REQUEST, "ERR0005", "Request not valid"),
    HTTP_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "ERR0006", "Method not allowed"),
    HTTP_INVALID_PARAM(HttpStatus.BAD_REQUEST, "ERR0007", "Invalid parameter"),
    HTTP_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "ERR0008", "Unauthorized Request"),
    HTTP_FORBIDDEN(HttpStatus.FORBIDDEN, "ERR0009", "Forbidden"),

    JOB_SCHEDULER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "ERR9001", "Scheduler exception error"),

    HTTP_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ERR9002", "Internal server error");

    private final HttpStatus status;
    private final String errorCode;
    private final String errorMessage;
}
