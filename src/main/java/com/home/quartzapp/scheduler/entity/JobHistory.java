package com.home.quartzapp.scheduler.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobHistory {
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
