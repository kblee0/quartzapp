package com.home.quartzapp.quartzjobs.common;

import com.home.quartzapp.common.exception.ErrorCodeException;
import com.home.quartzapp.common.util.ApplicationContextProvider;
import com.home.quartzapp.quartzjobs.util.JobDataMapWrapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
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

        String batchJobName = jobDataMap.getString("batchJobName").orElseThrow(() -> new ErrorCodeException("QJB0001", "bathJobNmae"));

        log.info("{} :: [JOB_START] batchJobName: {}", jobName, batchJobName);

        JobExecution jobExecution;
        try {
            Job batchJob = ApplicationContextProvider.getBeansOfType(Job.class).values().stream()
                    .filter(job -> job.getName().equals(batchJobName)).findFirst()
                    .orElseThrow(() -> new ErrorCodeException("QJBE0007", batchJobName));

            JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();

            jobDataMap.getJobDataMap().forEach((key, value) -> jobParametersBuilder.addJobParameter(key, new JobParameter(value, value.getClass())));

            JobLauncher jobLauncher = ApplicationContextProvider.getBean("jobLauncher", JobLauncher.class);
            jobExecution = jobLauncher.run(batchJob, jobParametersBuilder.toJobParameters());
        } catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException | JobParametersInvalidException | JobRestartException e) {
            throw new ErrorCodeException("QJBE0008", e, batchJobName);
        }

        stopWatch.stop();
        log.info("{} :: [JOB_FINISH] {}, exitStatus: {}", jobName, stopWatch.shortSummary(), jobExecution.getExitStatus());
    }
}
