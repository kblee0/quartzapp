package com.home.quartzapp.scheduler.dto;

import java.util.Date;

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
    Date startTime;
    Date endTime;
    String status;
    String exitMessage;
}
