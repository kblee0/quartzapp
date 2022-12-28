package com.home.quartzapp.scheduler.service;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.springframework.stereotype.Component;

@Component
public class TriggerListener implements org.quartz.TriggerListener {

    @Override
    public String getName() {
        return "globalTrigger";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        // false: Job Execute
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
    }
    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
            CompletedExecutionInstruction triggerInstructionCode) {
    }
    
}
