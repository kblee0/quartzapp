package com.home.quartzapp.scheduler.service;

import com.home.quartzapp.common.util.ExceptionUtil;
import com.home.quartzapp.scheduler.model.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrtzGlobalJobListener implements org.quartz.JobListener {
    private final QrtzJobHistoryService qrtzJobHistoryService;

    @Override
    public String getName() {
        return "QrtzGlobalJobListener";
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        log.debug("{} :: id: {}, jobToBeExecuted.", context.getJobDetail().getKey(), context.getFireInstanceId());
        qrtzJobHistoryService.saveQrtzJobHistory(context);
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        log.debug("jobWasExecuted :: JobKey: {}, InstanceId: {}, Status: {}",
            context.getJobDetail().getKey(),
            context.getFireInstanceId(),
            jobException == null ? JobStatus.COMPLETED.name() : JobStatus.FAILED.name());

        if(jobException != null) {
            ExceptionUtil.log(jobException);
        }
        qrtzJobHistoryService.updateQrtzJobHistory(context, jobException);
    }
}
