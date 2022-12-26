package com.home.quartzapp.scheduler.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class JobStatus {
	private JobInfo jobInfo;

	String jobState;

	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
	private LocalDateTime lastFiredTime;
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
	private LocalDateTime nextFireTime;

	private final LocalDateTime timestamp = LocalDateTime.now();
}
