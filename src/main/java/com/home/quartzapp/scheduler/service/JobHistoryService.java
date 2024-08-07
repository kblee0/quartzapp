package com.home.quartzapp.scheduler.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import com.home.quartzapp.scheduler.entity.JobHistory;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.quartzapp.scheduler.dto.JobHistoryDto;
import com.home.quartzapp.scheduler.repository.JobHistoryRepository;
import com.home.quartzapp.scheduler.model.JobStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class JobHistoryService {
    private final JobHistoryRepository jobHistoryRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    public void insertJobHistory(JobExecutionContext context) {
        JobHistoryDto jobHistoryDto = createJobHistory(context, JobStatus.STARTED);

        jobHistoryRepository.insertJobHistory(modelMapper.map(jobHistoryDto, JobHistory.class));
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

        jobHistoryRepository.updateJobHistory(modelMapper.map(jobHistoryDto, JobHistory.class));
    }

    private JobHistoryDto createJobHistory(JobExecutionContext context, JobStatus jobStatus) {
        JobHistoryDto jobHistoryDto = new JobHistoryDto();

        try {
            jobHistoryDto.setSchedName(context.getScheduler().getSchedulerName());
            jobHistoryDto.setEntryId(context.getFireInstanceId());
            jobHistoryDto.setTriggerName(context.getTrigger().getKey().getName());
            jobHistoryDto.setTriggerGroup(context.getTrigger().getKey().getGroup());
            jobHistoryDto.setJobName(context.getJobDetail().getKey().getName());
            jobHistoryDto.setJobGroup(context.getJobDetail().getKey().getGroup());
            jobHistoryDto.setStartTime(context.getFireTime());
            jobHistoryDto.setStatus(jobStatus.name());
            jobHistoryDto.setJobData(objectMapper.writeValueAsString(context.getJobDetail().getJobDataMap()));
        } catch (SchedulerException e) {
            log.error("createJobHistory :: {}", e.getMessage());
            return null;
        } catch (JsonProcessingException e) {
            log.error("jobDataMap writeValueAsString error :: jobDataMap: {}",
                context.getJobDetail().getJobDataMap(), 
                e);
        }

        return jobHistoryDto;
    }
}
