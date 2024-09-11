package com.home.quartzapp.batch.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.Map;

@Slf4j
public class LogTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Map<String,?> jobParameters = chunkContext.getStepContext().getJobParameters();

        log.info("---------------------------------------");
        log.info("logTasklet started");
        jobParameters.forEach((k,v)-> log.info(" + {}: {}", k, v) );
        log.info("---------------------------------------");

        chunkContext.getStepContext().getStepExecution().setReadCount(10);
        chunkContext.getStepContext().getStepExecution().setWriteCount(10);

        return RepeatStatus.FINISHED;
    }
}
