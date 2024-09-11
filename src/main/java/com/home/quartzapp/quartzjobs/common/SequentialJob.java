package com.home.quartzapp.quartzjobs.common;

import com.home.quartzapp.common.exception.ErrorCodeException;
import com.home.quartzapp.quartzjobs.util.JobDataMapWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

/*
    - JobDataMap Sample

    "jobDataMap": {
        "jobSequence": [
            {
                "jobClassName": "com.home.quartzapp.quartzjobs.common.CommandJob",
                "jobDataMap": {
                    "cwd": "c:\\home\\proj\\quartz",
                    "command": "echo SequentialJob - 1",
                    "outputToLog": true
                },
                "stopOnError": true
            },
            {
                "jobClassName": "com.home.quartzapp.quartzjobs.common.CommandJob",
                "jobDataMap": {
                    "command": "echo SequentialJob - 2",
                    "outputToLog": true
                }
            }
        ]
    }
*/


@Getter
@Setter
@Slf4j
@Component
public class SequentialJob extends QuartzJobBean {
    private String jobName;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        this.setJobName(context.getJobDetail().getKey().toString());

        // StopWatch - start
        StopWatch stopWatch = new StopWatch(context.getFireInstanceId());

        // JobDataMap Check
        JobDataMapWrapper jobDataMap = new JobDataMapWrapper(context.getMergedJobDataMap());

        log.info("{} :: [JOB_START]", jobName);

        List<JobDataMap> jobSequence = jobDataMap.get("jobSequence", List.class).orElseThrow(() -> new ErrorCodeException("QJBE0001", "jobSequence"));

        for(Map<String,Object> jobMap : jobSequence) {
            JobDataMapWrapper job = new JobDataMapWrapper(new JobDataMap(jobMap));

            String jobClassName = job.getString("jobClassName").orElseThrow(() -> new ErrorCodeException("QJBE0001", "jobClassName"));
            boolean stopOnError = job.getBoolean("stopOnError").orElse(true);
            Map<String,Object> subJobDataMap = job.get("jobDataMap", Map.class).orElse(null);
            Object prevJobResult = job.get("result",Object.class).orElse(null);

            stopWatch.start(jobName.concat("->").concat(jobClassName));

            // JobDataMap change
            context.getJobDetail().getJobDataMap().clear();
            context.getMergedJobDataMap().clear();

            if(subJobDataMap != null) {
                JobDataMapWrapper convSubJobDataMap = new JobDataMapWrapper(subJobDataMap);
                context.getJobDetail().getJobDataMap().putAll(convSubJobDataMap.getJobDataMap());
                context.getMergedJobDataMap().putAll(convSubJobDataMap.getJobDataMap());
            }
            if(prevJobResult != null) {
                context.getMergedJobDataMap().put("result", prevJobResult);
            }
            log.info("{} || [SUB_JOB_EXEC] subJobClassName: {}, stopOnError = {}", jobName, jobClassName, stopOnError);

            try {
                this.jobExecute(context, jobClassName);
            } catch (Exception e) {
                if(stopOnError) throw new ErrorCodeException("QJBE0002", e);
                log.warn("{} :: An error occurred in server job \"{}}\".", jobName, jobClassName);
            }
            stopWatch.stop();
        }
        log.info("{} :: [JOB_FINISH] {}\n{}", jobName, stopWatch.shortSummary(), stopWatch.prettyPrint());
    }

    private void jobExecute(JobExecutionContext context, String className ) throws JobExecutionException {
        Class<?> jobClass;
        Job jobInstance;
        try {
            jobClass = Class.forName(className);
            Constructor<?> constructor = jobClass.getDeclaredConstructor();
            constructor.setAccessible(true); // If constructor is private
            jobInstance = (Job)constructor.newInstance();
        } catch (Exception e) {
            throw new ErrorCodeException("QJBE0005", e, className);
        }
        jobInstance.execute(context);
    }
}
