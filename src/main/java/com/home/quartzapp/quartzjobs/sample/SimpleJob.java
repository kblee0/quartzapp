package com.home.quartzapp.quartzjobs.sample;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Slf4j
public class SimpleJob extends QuartzJobBean {
	private final int MAX_SLEEP_IN_SECONDS = 3;

	private volatile Thread currThread;

	@Override
	protected void executeInternal(JobExecutionContext context) {
		JobKey jobKey = context.getJobDetail().getKey();
		currThread = Thread.currentThread();
		log.info("============================================================================");
		log.info("SimpleJob started :: jobKey : {} - {}", jobKey, currThread.getName());

		IntStream.range(0, 2).forEach(i -> {
			log.info("SimpleJob Counting - {}", i);
			try {
				TimeUnit.SECONDS.sleep(MAX_SLEEP_IN_SECONDS);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		});

		log.info("SimpleJob ended :: jobKey : {} - {}", jobKey, currThread.getName());
		log.info("============================================================================");
	}
}