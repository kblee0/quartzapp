package com.home.quartzapp.scheduler.dto;

import java.util.Date;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
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
STATUS VARCHAR(16) NOT NULL,
EXIT_MESSAGE VARCHAR(2500) NULL,
PRIMARY KEY (SCHED_NAME,ENTRY_ID))
ENGINE=InnoDB;

CREATE INDEX IDX_QRTZ_JH_J_G_S ON QRTZ_JOB_HISTORY(JOB_NAME,JOB_GROUP,START_TIME);
*/