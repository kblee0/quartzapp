package com.home.quartzapp.scheduler.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.home.quartzapp.scheduler.dao.JobHistoryDAO;
import com.home.quartzapp.scheduler.entity.JobHistory;
import com.home.quartzapp.scheduler.model.JobStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JobHistoryService {
    @Autowired
    private JobHistoryDAO jobHistoryDAO;

    public void insertJobHistory(JobExecutionContext context) {
        JobHistory jobHistory = createJobHistory(context, JobStatus.STARTED);

        jobHistoryDAO.insertJobHistory(jobHistory);
    }

    public void updateJobHistory(JobExecutionContext context, JobExecutionException jobException) {
        JobHistory jobHistory = createJobHistory(context, JobStatus.COMPLETED);

        jobHistory.setEndTime(new Date());

        if(jobException != null) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            jobException.printStackTrace(printWriter);

            jobHistory.setStatus(JobStatus.FAILED.name());
            jobHistory.setExitMessage(stringWriter.toString());
        }

        jobHistoryDAO.updateJobHistory(jobHistory);
    }

    private JobHistory createJobHistory(JobExecutionContext context, JobStatus jobStatus) {
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
            jobHistory.setStatus(jobStatus.name());
        } catch (SchedulerException e) {
            log.error("createJobHistory :: {}", e);
            return null;
        }

        return jobHistory;
    }
}
