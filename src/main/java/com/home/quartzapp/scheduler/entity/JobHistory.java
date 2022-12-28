package com.home.quartzapp.scheduler.entity;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
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
    String state;
    String result;

    public static JobHistory createJobHistory(JobExecutionContext context) {
        JobHistory jobHistory = new JobHistory();

        try {
            jobHistory.setSchedName(context.getScheduler().getSchedulerName());
            jobHistory.setEntryId(context.getFireInstanceId());
            jobHistory.setTriggerName(context.getTrigger().getKey().getName());
            jobHistory.setTriggerGroup(context.getTrigger().getKey().getGroup());
            jobHistory.setJobName(context.getJobDetail().getKey().getName());
            jobHistory.setJobGroup(context.getJobDetail().getKey().getGroup());
            jobHistory.setJobData(context.getJobDetail().getJobDataMap().toString());
            jobHistory.setStartTime(context.getFireTime());
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        return jobHistory;
    }
}

/*
CREATE TABLE QRTZ_JOB_HISTORY (
SCHED_NAME VARCHAR(120) NOT NULL,
ENTRY_ID VARCHAR(95) NOT NULL,
TRIGGER_NAME VARCHAR(190) NOT NULL,
TRIGGER_GROUP VARCHAR(190) NOT NULL,
JOB_NAME VARCHAR(190) NULL,
JOB_GROUP VARCHAR(190) NULL,
JOB_DATA VARCHAR(4096) NULL,
START_TIME DATETIME NOT NULL,
END_TIME DATETIME NULL,
STATE VARCHAR(16) NOT NULL,
RESULT TEXT NULL,
PRIMARY KEY (SCHED_NAME,ENTRY_ID))
ENGINE=InnoDB;

CREATE INDEX IDX_QRTZ_JH_J_G_S ON QRTZ_JOB_HISTORY(JOB_NAME,JOB_GROUP,START_TIME);
*/