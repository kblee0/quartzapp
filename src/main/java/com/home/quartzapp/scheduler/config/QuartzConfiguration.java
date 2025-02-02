package com.home.quartzapp.scheduler.config;


import java.util.Properties;

import javax.sql.DataSource;

import com.home.quartzapp.scheduler.service.QrtzGlobalJobListener;
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

import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class QuartzConfiguration {
    private final QrtzGlobalJobListener qrtzGlobalJobListener;

    private static class AutoWiringSpringBeanJobFactory extends SpringBeanJobFactory {
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
    SchedulerFactoryBean schedulerFactoryBean(
            DataSource dataSource,
            QuartzProperties quartzProperties,
            PlatformTransactionManager transactionManager,
            ApplicationContext applicationContext) {

        AutoWiringSpringBeanJobFactory  jobFactory = new AutoWiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);

        Properties properties = new Properties();
        properties.putAll(quartzProperties.getProperties());

        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setDataSource(dataSource);
        schedulerFactoryBean.setJobFactory(jobFactory);
        schedulerFactoryBean.setApplicationContext(applicationContext);
        schedulerFactoryBean.setQuartzProperties(properties);
        schedulerFactoryBean.setTransactionManager(transactionManager);

        schedulerFactoryBean.setOverwriteExistingJobs(true);
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
        schedulerFactoryBean.setAutoStartup(true);
        schedulerFactoryBean.setStartupDelay(3);
        schedulerFactoryBean.setGlobalJobListeners(qrtzGlobalJobListener);

        return schedulerFactoryBean;
    }
}
