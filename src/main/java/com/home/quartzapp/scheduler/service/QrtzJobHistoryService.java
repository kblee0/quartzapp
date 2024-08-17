package com.home.quartzapp.scheduler.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

import com.home.quartzapp.common.util.DateTimeUtil;
import com.home.quartzapp.common.util.ExceptionUtil;
import com.home.quartzapp.scheduler.entity.JobHistory;
import com.home.quartzapp.scheduler.entity.QrtzJobHistory;
import com.home.quartzapp.scheduler.entity.QrtzJobHistoryId;
import com.home.quartzapp.scheduler.repository.QrtzJobHistoryRepository;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class QrtzJobHistoryService {
    private final JobHistoryRepository jobHistoryRepository;
    private final QrtzJobHistoryRepository qrtzJobHistoryRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    public void saveQrtzJobHistory(JobExecutionContext context) {
        try {
            QrtzJobHistoryId qrtzJobHistoryId = QrtzJobHistoryId.builder()
                    .schedName(context.getScheduler().getSchedulerName())
                    .entryId(context.getFireInstanceId())
                    .build();
            QrtzJobHistory qrtzJobHistory = QrtzJobHistory.builder()
                    .id(qrtzJobHistoryId)
                    .triggerName(context.getTrigger().getKey().getName())
                    .triggerGroup(context.getTrigger().getKey().getGroup())
                    .jobName(context.getJobDetail().getKey().getName())
                    .jobGroup(context.getJobDetail().getKey().getGroup())
                    .startTime(DateTimeUtil.toLocalDateTime(context.getFireTime()))
                    .status(JobStatus.STARTED.name())
                    .jobData(objectMapper.writeValueAsString(context.getTrigger().getJobDataMap()))
                    .build();
            qrtzJobHistoryRepository.save(qrtzJobHistory);
//        } catch (SchedulerException e) {
//            log.error("saveQrtzJobHistory :: {}, {}",  e.getMessage(), ExceptionUtil.getStackTrace(e));
//        } catch (JsonProcessingException e) {
//            log.error("saveQrtzJobHistory :: {}, {}",  e.getMessage(), ExceptionUtil.getStackTrace(e));
        } catch (Exception e) {
            log.error("saveQrtzJobHistory :: {}, {}",  e.getMessage(), ExceptionUtil.getStackTrace(e));
        }
    }

    public void updateQrtzJobHistory(JobExecutionContext context, JobExecutionException jobException) {
        try {
            StringBuilder exitMessage = new StringBuilder();

            if(jobException != null) {
                exitMessage.append(jobException.getMessage());
                if(jobException.getCause() != null) exitMessage.append(" :: ").append(jobException.getCause().getMessage());
            }
            qrtzJobHistoryRepository.updateEndTimeAndStatusAndExitMessageById(
                    LocalDateTime.now(),
                    jobException == null ? JobStatus.COMPLETED.name() : JobStatus.FAILED.name(),
                    exitMessage.isEmpty() ? null : exitMessage.toString(),
                    QrtzJobHistoryId.builder()
                            .schedName(context.getScheduler().getSchedulerName())
                            .entryId(context.getFireInstanceId()).build());
        // } catch (SchedulerException e) {
        //    log.error("updateQrtzJobHistory :: {}, {}", e.getMessage(), ExceptionUtil.getStackTrace(e));
        } catch (Exception e) {
            log.error("updateQrtzJobHistory :: {}, {}",  e.getMessage(), ExceptionUtil.getStackTrace(e));
        }
    }

    public void insertJobHistory(JobExecutionContext context) {
        JobHistoryDto jobHistoryDto = createJobHistory(context, JobStatus.STARTED);

        jobHistoryRepository.insertJobHistory(modelMapper.map(jobHistoryDto, JobHistory.class));
    }

    public void updateJobHistory(JobExecutionContext context, JobExecutionException jobException) {
        JobHistoryDto jobHistoryDto = createJobHistory(context, JobStatus.COMPLETED);

        jobHistoryDto.setEndTime(LocalDateTime.now());

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
            jobHistoryDto.setStartTime(DateTimeUtil.toLocalDateTime(context.getFireTime()));
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
