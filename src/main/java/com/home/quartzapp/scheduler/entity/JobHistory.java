package com.home.quartzapp.scheduler.entity;

import java.util.Date;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class JobHistory {
    String schedName;
    String triggerName;
    String triggerGroup;
    String jobName;
    String jobGroup;
    String jobData;
    Date startTime;
    Date endTime;
}
