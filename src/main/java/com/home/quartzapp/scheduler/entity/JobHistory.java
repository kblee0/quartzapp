package com.home.quartzapp.scheduler.entity;

import lombok.Data;

import java.util.Date;

@Data
public class JobHistory {
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
