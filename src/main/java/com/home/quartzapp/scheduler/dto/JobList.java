package com.home.quartzapp.scheduler.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
public class JobList {
	private int numOfAllJobs;
	private int numOfGroups;
	private int numOfRunningJobs;
	private List<JobStatus> jobs;
}
