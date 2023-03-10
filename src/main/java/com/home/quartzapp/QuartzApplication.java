package com.home.quartzapp;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class QuartzApplication {

	@Autowired
    private SchedulerFactoryBean schedulerFactoryBean;
	
	public static void main(String[] args) {
		SpringApplication.run(QuartzApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() throws SchedulerException {
		log.info("===========================================================");
		log.info("- Application ready....");
		log.info("- Starting scheduler...");
		schedulerFactoryBean.getScheduler().start();
		log.info("===========================================================");
	}
}