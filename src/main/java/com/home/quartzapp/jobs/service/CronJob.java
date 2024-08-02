package com.home.quartzapp.jobs.service;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Slf4j
public class CronJob extends QuartzJobBean {
	private final int MAX_SLEEP_IN_SECONDS = 5;

	private volatile Thread currThread;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
		if (!jobDataMap.isEmpty()) {
			// int jobId = jobDataMap.getInt("jobId");
			JobKey jobKey = context.getJobDetail().getKey();
			TriggerKey triggerKey = context.getTrigger().getKey();

			currThread = Thread.currentThread();
			log.info("============================================================================");
			log.info("CronJob started :: sleep : {} : jobKey : {} : TriggerKey : {} : TM : {}", MAX_SLEEP_IN_SECONDS, jobKey, triggerKey, currThread.getName());

			IntStream.range(0, 3).forEach(i -> {
				log.info("CronJob Counting - {}", i);
				try {
					TimeUnit.SECONDS.sleep(MAX_SLEEP_IN_SECONDS);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			});
			log.info("CronJob ended :: jobKey : {} - {}", jobKey, currThread.getName());
			log.info("============================================================================");
		}
	}
}
