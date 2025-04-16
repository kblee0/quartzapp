package com.home.quartzapp.scheduler.service;

import com.home.quartzapp.common.util.ExceptionUtil;
import com.home.quartzapp.scheduler.constant.JobStatus;
import com.home.quartzapp.scheduler.constant.TriggerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

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
            jobException == null ? JobStatus.COMPLETED : JobStatus.FAILED);

        if(jobException != null) {
            ExceptionUtil.log(jobException);
        }
        qrtzJobHistoryService.updateQrtzJobHistory(context, jobException);

        Trigger trigger;
        try {
            trigger = context.getScheduler().getTrigger(context.getTrigger().getKey());
        } catch (SchedulerException e) {
            log.error("jobWasExecuted.getTrigger > Exception > ERROR:: triggerKey: {}", context.getTrigger().getKey(), e);
            throw new IllegalArgumentException("jobWasExecuted SchedulerException", e);
        }

        if (TriggerType.isFixedTriggerType(trigger)) {
            SimpleTrigger simpleTrigger = (SimpleTrigger)trigger;
            JobDataMap jobDataMap = simpleTrigger.getJobDataMap();

            Timestamp newStartTime = new Timestamp(System.currentTimeMillis() + simpleTrigger.getRepeatInterval());
            jobDataMap.put("previousFireTime", simpleTrigger.getStartTime());

            SimpleTrigger newTrigger = simpleTrigger.getTriggerBuilder()
                    .startAt(newStartTime)
                    .usingJobData(jobDataMap)
                    .build();

            try {
                context.getScheduler().rescheduleJob(simpleTrigger.getKey(), newTrigger);
            } catch (SchedulerException e) {
                log.error("jobWasExecuted.fixed_delay > Exception > ERROR:", e);
                throw new IllegalArgumentException("jobWasExecuted SchedulerException", e);
            }
        }
    }
}
