package com.home.quartzapp.scheduler.service;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JobListener implements org.quartz.JobListener {
    @Override
    public String getName() {
        return "globalJob";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        log.info("jobToBeExecuted at :: jobKey : {}", context.getJobDetail().getKey());
        
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        log.info("jobExecutionVetoed :: jobKey : {}", context.getJobDetail().getKey());
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        log.info("jobWasExecuted :: jobKey : {}", context.getJobDetail().getKey());
    }
}
