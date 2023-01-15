package com.home.quartzapp.scheduler.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.quartzapp.scheduler.dto.JobHistoryDto;
import com.home.quartzapp.scheduler.mapper.JobHistoryMapper;
import com.home.quartzapp.scheduler.model.JobStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JobHistoryService {
    @Autowired
    private JobHistoryMapper jobHistoryMapper;

    public void insertJobHistory(JobExecutionContext context) {
        JobHistoryDto jobHistoryDto = createJobHistory(context, JobStatus.STARTED);

        jobHistoryMapper.insertJobHistory(jobHistoryDto);
    }

    public void updateJobHistory(JobExecutionContext context, JobExecutionException jobException) {
        JobHistoryDto jobHistoryDto = createJobHistory(context, JobStatus.COMPLETED);

        jobHistoryDto.setEndTime(new Date());

        if(jobException != null) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            jobException.printStackTrace(printWriter);

            jobHistoryDto.setStatus(JobStatus.FAILED.name());
            jobHistoryDto.setExitMessage(stringWriter.toString());
        }

        jobHistoryMapper.updateJobHistory(jobHistoryDto);
    }

    private JobHistoryDto createJobHistory(JobExecutionContext context, JobStatus jobStatus) {
        JobHistoryDto jobHistoryDto = new JobHistoryDto();
        ObjectMapper mapper = new ObjectMapper();

        try {
            jobHistoryDto.setSchedName(context.getScheduler().getSchedulerName());
            jobHistoryDto.setEntryId(context.getFireInstanceId());
            jobHistoryDto.setTriggerName(context.getTrigger().getKey().getName());
            jobHistoryDto.setTriggerGroup(context.getTrigger().getKey().getGroup());
            jobHistoryDto.setJobName(context.getJobDetail().getKey().getName());
            jobHistoryDto.setJobGroup(context.getJobDetail().getKey().getGroup());
            jobHistoryDto.setStartTime(context.getFireTime());
            jobHistoryDto.setStatus(jobStatus.name());
            jobHistoryDto.setJobData(mapper.writeValueAsString(context.getJobDetail().getJobDataMap()));
        } catch (SchedulerException e) {
            log.error("createJobHistory :: {}", e);
            return null;
        } catch (JsonProcessingException e) {
            log.error("jobDataMap writeValueAsString error :: jobDataMap: {}, {}",
                context.getJobDetail().getJobDataMap(), 
                e);
        }

        return jobHistoryDto;
    }
}
