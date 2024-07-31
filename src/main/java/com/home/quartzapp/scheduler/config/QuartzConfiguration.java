package com.home.quartzapp.scheduler.config;


import java.util.Properties;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import com.home.quartzapp.scheduler.service.JobListener;
import com.home.quartzapp.scheduler.service.TriggerListener;

@Configuration
@RequiredArgsConstructor
public class QuartzConfiguration {
    private final TriggerListener triggerListener;
    private final JobListener jobListener;
	private final QuartzProperties quartzProperties;
	private final DataSource dataSource;
    
    public class AutowireCapableBeanJobFactory extends SpringBeanJobFactory {

        private transient AutowireCapableBeanFactory beanFactory;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
        }

        @Override
        protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
            Object jobInstance = super.createJobInstance(bundle);
            this.beanFactory.autowireBean(jobInstance);
            // this.beanFactory.initializeBean(jobInstance, null);
            return jobInstance;
        }
    }

    @Bean
    SchedulerFactoryBean schedulerFactoryBean(ApplicationContext applicationContext) {
        AutowireCapableBeanJobFactory jobFactory = new AutowireCapableBeanJobFactory();
        Properties properties = new Properties();
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

        properties.putAll(quartzProperties.getProperties());

        jobFactory.setApplicationContext(applicationContext);

        schedulerFactoryBean.setGlobalTriggerListeners(triggerListener);
        schedulerFactoryBean.setGlobalJobListeners(jobListener);
        schedulerFactoryBean.setOverwriteExistingJobs(true);
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
        schedulerFactoryBean.setApplicationContext(applicationContext);
        schedulerFactoryBean.setDataSource(dataSource);
        schedulerFactoryBean.setJobFactory(jobFactory);
        schedulerFactoryBean.setQuartzProperties(properties);
        schedulerFactoryBean.setAutoStartup(false);

        return schedulerFactoryBean;
    }
}
