package com.home.quartzapp;

import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class QuartzApplication {
	private final int QUARTZ_START_DELAY_SECONDS = 3;

    private final SchedulerFactoryBean schedulerFactoryBean;
	
	public static void main(String[] args) {
		SpringApplication.run(QuartzApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() throws SchedulerException {
		log.info("===========================================================");
		log.info("- Application ready....");
		try {
			schedulerFactoryBean.getScheduler().startDelayed(QUARTZ_START_DELAY_SECONDS);
			log.info("- Quartz scheduler will start in {} seconds....", QUARTZ_START_DELAY_SECONDS);
		} catch (SchedulerException e) {
			log.error("Quartz scheduler start failed.", e);
			throw e;
		}
		log.info("-----------------------------------------------------------");
	}
}