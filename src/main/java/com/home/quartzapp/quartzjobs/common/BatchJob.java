package com.home.quartzapp.quartzjobs.common;

import com.home.quartzapp.common.exception.ErrorCodeException;
import com.home.quartzapp.common.util.ApplicationContextProvider;
import com.home.quartzapp.quartzjobs.util.JobDataMapWrapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Setter
@Component
public class BatchJob extends QuartzJobBean {
    private String jobName;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        this.setJobName(context.getJobDetail().getKey().toString());

        // StopWatch - start
        StopWatch stopWatch = new StopWatch(context.getFireInstanceId());
        stopWatch.start(jobName);

        // JobDataMap Check
        JobDataMapWrapper jobDataMap = new JobDataMapWrapper(context.getMergedJobDataMap());

        String batchJobName = jobDataMap.getString("batchJobName")
                .orElseThrow(() -> new ErrorCodeException("QJB0001", "batchJobName"));

        log.info("{} :: [JOB_START] batchJobName: {}", jobName, batchJobName);

        JobExecution jobExecution;
        try {
            Job batchJob = ApplicationContextProvider.getBeansOfType(Job.class).values().stream()
                    .filter(job -> job.getName().equals(batchJobName))
                    .findFirst()
                    .orElseThrow(() -> new ErrorCodeException("QJBE0007", batchJobName));

            JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
            jobDataMap.getJobDataMap().forEach((key, value) ->
                    jobParametersBuilder.addJobParameter(
                            new JobParameter<>(key, value, (Class<Object>) value.getClass())
                    )
            );
            JobParameters jobParameters = jobParametersBuilder.toJobParameters();

            JobOperator jobOperator = ApplicationContextProvider.getBean("jobOperator", JobOperator.class);
            jobExecution = jobOperator.start(batchJob, jobParameters);

        } catch (Exception e) {
            throw new ErrorCodeException("QJBE0008", e, batchJobName);
        }

        stopWatch.stop();
        log.info("{} :: [JOB_FINISH] {}, exitStatus: {}", jobName, stopWatch.shortSummary(), jobExecution.getExitStatus());
    }
}