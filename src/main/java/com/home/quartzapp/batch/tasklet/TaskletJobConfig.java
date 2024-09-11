package com.home.quartzapp.batch.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class TaskletJobConfig {
    @Bean
    public Job taskletJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("taskletJob", jobRepository)
                .start(taskletStep(jobRepository, transactionManager))
                .build();
    }
    @Bean
    public Step taskletStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("taskletStep", jobRepository)
                .tasklet(logTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet logTasklet() {
        return new LogTasklet();
    }
}
