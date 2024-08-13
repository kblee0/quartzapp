package com.home.quartzapp.scheduler.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class JobHistoryDto {
    String schedName;
    String entryId;
    String triggerName;
    String triggerGroup;
    String jobName;
    String jobGroup;
    String jobData;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String status;
    String exitMessage;
}
