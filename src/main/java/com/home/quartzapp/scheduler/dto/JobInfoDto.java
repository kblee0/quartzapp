package com.home.quartzapp.scheduler.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import org.quartz.JobDataMap;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
