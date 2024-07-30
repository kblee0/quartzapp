package com.home.quartzapp.common.exception;

import java.time.LocalDateTime;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ApiErrorDto {
    private String errorCode;
    private String errorMessage;
    private final LocalDateTime timestamp = LocalDateTime.now();
}
