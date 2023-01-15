package com.home.quartzapp.scheduler.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class JobStatusDto {
	private JobInfoDto jobInfoDto;

	String jobState;

	private LocalDateTime lastFiredTime;
	private LocalDateTime nextFireTime;

	private final LocalDateTime timestamp = LocalDateTime.now();
}
