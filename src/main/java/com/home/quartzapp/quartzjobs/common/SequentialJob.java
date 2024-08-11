package com.home.quartzapp.quartzjobs.common;

import com.home.quartzapp.common.exception.ApiException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;

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
public class SequentialJob extends QuartzJobBean {
    private String jobName;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        this.setJobName(context.getJobDetail().getKey().toString());

        log.info("{} :: [JOB_START]", jobName);

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

        ArrayList<?> jobSequence = Optional.ofNullable((ArrayList<Object>)jobDataMap.get("jobSequence")).orElse(new ArrayList<>());

        if(jobSequence.isEmpty()) log.warn("jobSequence is not defined : JobName: {}", jobName);

        for(Object j : jobSequence) {
            LinkedHashMap<String,?> jobData = (LinkedHashMap<String,?>)j;
            String jobClassName = jobData.get("jobClassName").toString();

            // JobDataMap change
            HashMap<String,?> subJobDataMap = (HashMap<String,?>)jobData.get("jobDataMap");
            Boolean stopOnError = Optional.ofNullable((Boolean)subJobDataMap.get("stopOnError")).orElse(true);
            context.getJobDetail().getJobDataMap().clear();
            context.getMergedJobDataMap().clear();
            if(subJobDataMap != null) {
                context.getJobDetail().getJobDataMap().putAll(subJobDataMap);
                context.getMergedJobDataMap().putAll(subJobDataMap);
            }
            log.info("{} || [SUB_JOB_EXEC] subJobClassName: {}, stopOnError = {}", jobName, jobClassName, stopOnError);

            try {
                this.jobExecute(context, jobClassName);
            } catch (Exception e) {
                if(stopOnError) throw e;
                ApiException.code("SCHE0004").log(e);
            }
        }
        log.info("{} :: [JOB_FINISH]", jobName);
    }
    private void jobExecute(JobExecutionContext context, String className ) {
        Class<?> jobClass;
        Job jobInstance;
        try {
            jobClass = Class.forName(className);
            Constructor<?> constructor = jobClass.getDeclaredConstructor();
            constructor.setAccessible(true); // If constructor is private
            jobInstance = (Job)constructor.newInstance();
        } catch (ClassNotFoundException e) {
            log.error("{} :: ClassNotFoundException, className = {}", jobName, className );
            throw new RuntimeException(e);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            log.error("{} :: InstantiationException, className = {}, message = {}", jobName, className, e.getMessage() );
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            log.error("{} :: IllegalAccessException, className = {}, message = {}", jobName, className, e.getMessage() );
            throw new RuntimeException(e);
        }
        try {
            jobInstance.execute(context);
        } catch (JobExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
