package com.home.quartzapp.batch.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BatchJobExecution {
    private String jobName;
    private String jobExecutionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String exitCode;
    private String exitMessage;
}
