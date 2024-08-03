package com.home.quartzapp.scheduler.dto;

import lombok.Builder;
import lombok.Data;
import org.quartz.JobDataMap;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
public class JobInfoDto {
    // JobDetail
    @NotBlank(message = "Group name cannot be empty...")
    private String group;
    @NotBlank
    private String name;
    private String description;
    private String jobClassName;
    private JobDataMap jobDataMap;
    private boolean interruptible;

    private Set<JobTriggerDto> triggers;
}
