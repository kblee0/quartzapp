package com.home.quartzapp.scheduler.service;

import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.home.quartzapp.scheduler.model.JobStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrtzJobHistoryListener implements org.quartz.JobListener {
    private final QrtzJobHistoryService qrtzJobHistoryService;

    @Override
    public String getName() {
        return "QrtzJobHistoryListener";
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
        log.debug("{} :: id: {}, result: {}, jobWasExecuted.",
            context.getJobDetail().getKey(),
            context.getFireInstanceId(),
            jobException == null ? JobStatus.COMPLETED.name() : JobStatus.FAILED.name());

            qrtzJobHistoryService.updateQrtzJobHistory(context, jobException);
    }
}
