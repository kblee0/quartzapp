package com.home.quartzapp.scheduler.service;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TriggerListener implements org.quartz.TriggerListener {

    @Override
    public String getName() {
        return "globalTrigger";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        log.info("triggerFired at {} :: jobKey : {}, triggerKey : {}, jobDataMpa : {}", 
            trigger.getStartTime(),
            trigger.getJobKey(),
            trigger.getKey(),
            trigger.getJobDataMap());
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        log.info("vetoJobExecution at {} :: jobKey : {}, triggerKey : {}", 
            trigger.getStartTime(),
            trigger.getJobKey(),
            trigger.getKey());
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        log.info("triggerMisfired at {} :: jobKey : {}, triggerKey : {}", 
            trigger.getStartTime(),
            trigger.getJobKey(),
            trigger.getKey());
    }
    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
            CompletedExecutionInstruction triggerInstructionCode) {
        log.info("triggerComplete at {} :: jobKey : {}, triggerKey : {}", 
        trigger.getStartTime(),
        trigger.getJobKey(),
        trigger.getKey());            
    }
    
}
