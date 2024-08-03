package com.home.quartzapp.scheduler.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JobListDto {
	private int numOfAllJobs;
	private int numOfGroups;
	private int numOfRunningJobs;
	private List<JobStatusDto> jobs;
}
