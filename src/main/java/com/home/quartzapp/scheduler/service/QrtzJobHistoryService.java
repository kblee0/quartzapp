package com.home.quartzapp.scheduler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.quartzapp.common.exception.ErrorCodeException;
import com.home.quartzapp.common.util.DateTimeUtil;
import com.home.quartzapp.common.util.ExceptionUtil;
import com.home.quartzapp.scheduler.entity.QrtzJobHistory;
import com.home.quartzapp.scheduler.entity.QrtzJobHistoryId;
import com.home.quartzapp.scheduler.constant.JobStatus;
import com.home.quartzapp.scheduler.repository.QrtzJobHistoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class QrtzJobHistoryService {
    private final QrtzJobHistoryRepository qrtzJobHistoryRepository;
    private final ObjectMapper objectMapper;

    public void saveQrtzJobHistory(JobExecutionContext context) {
        try {
            QrtzJobHistoryId qrtzJobHistoryId = QrtzJobHistoryId.builder()
                    .schedName(context.getScheduler().getSchedulerName())
                    .entryId(context.getFireInstanceId())
                    .build();
            QrtzJobHistory qrtzJobHistory = QrtzJobHistory.builder()
                    .id(qrtzJobHistoryId)
                    .jobName(context.getJobDetail().getKey().getName())
                    .jobGroup(context.getJobDetail().getKey().getGroup())
                    .startTime(DateTimeUtil.toLocalDateTime(context.getFireTime()))
                    .triggerName(context.getTrigger().getKey().getName())
                    .triggerGroup(context.getTrigger().getKey().getGroup())
                    .status(JobStatus.STARTED)
                    .jobData(objectMapper.writeValueAsString(context.getJobDetail().getJobDataMap()))
                    .build();
            qrtzJobHistoryRepository.save(qrtzJobHistory);
//        } catch (SchedulerException e) {
//            log.error("saveQrtzJobHistory :: {}, {}",  e.getMessage(), ExceptionUtil.getStackTrace(e));
//        } catch (JsonProcessingException e) {
//            log.error("saveQrtzJobHistory :: {}, {}",  e.getMessage(), ExceptionUtil.getStackTrace(e));
        } catch (Exception e) {
            throw new ErrorCodeException("QJBE0006", e, "job history");
        }
    }

    public void updateQrtzJobHistory(JobExecutionContext context, JobExecutionException jobException) {
        try {
            String exitCode;
            StringBuilder exitMessage = new StringBuilder();

            if(jobException == null) {
                exitCode = null;
            } else {
                ErrorCodeException errorCodeException = ExceptionUtil.findErrorCodeException(jobException);

                if(errorCodeException != null) {
                    exitCode = errorCodeException.getErrorCode();
                    exitMessage.append(errorCodeException.getMessage());
                    if (errorCodeException.getCause() != null)
                        exitMessage.append(" [").append(errorCodeException.getCause().getMessage()).append("]");
                } else {
                    exitCode = "QJBE9999";
                    if(jobException.getCause() instanceof SchedulerException schedulerException) {
                        exitMessage.append(schedulerException.getMessage());
                        if(schedulerException.getCause() != null)
                            exitMessage.append(" [").append(schedulerException.getCause().getMessage()).append("]");
                    } else {
                        exitMessage.append(jobException.getMessage());
                        if(jobException.getCause() != null) {
                            exitMessage.append(" [").append(jobException.getCause().getMessage()).append("]");
                        }
                    }
                }
            }

            QrtzJobHistoryId qrtzJobHistoryId = QrtzJobHistoryId.builder()
                    .schedName(context.getScheduler().getSchedulerName())
                    .entryId(context.getFireInstanceId())
                    .build();

            QrtzJobHistory qrtzJobHistory = qrtzJobHistoryRepository.findById(qrtzJobHistoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Job start history does not exist."));

            qrtzJobHistory.setStatus(jobException == null ? JobStatus.COMPLETED : JobStatus.FAILED);
            qrtzJobHistory.setEndTime(LocalDateTime.now());
            qrtzJobHistory.setExitCode(exitCode);
            qrtzJobHistory.setExitMessage(exitMessage.isEmpty() ? null : exitMessage.toString());

            qrtzJobHistoryRepository.save(qrtzJobHistory);
        } catch (Exception e) {
            throw new ErrorCodeException("QJBE0006", e, "job history");
        }
    }
}
