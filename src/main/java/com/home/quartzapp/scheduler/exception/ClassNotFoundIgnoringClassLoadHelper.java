package com.home.quartzapp.scheduler.exception;

import org.quartz.Job;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClassNotFoundIgnoringClassLoadHelper extends org.quartz.simpl.CascadingClassLoadHelper {
    @Override
    public Class<?> loadClass(String name) {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            log.error("{} : Quartz job class not found. Load it into the Job.class.", name);
            Class<Job> clz = Job.class;
            return clz;
        }
    }
}
