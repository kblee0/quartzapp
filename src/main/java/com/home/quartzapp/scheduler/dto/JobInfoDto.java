package com.home.quartzapp.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.quartz.JobDataMap;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
public class JobInfoDto {
    private final String group;
    @NotBlank
    private final String name;
    private String description;
    private final String jobClassName;
    private JobDataMap jobDataMap;
    private boolean interruptible;

    private Set<JobTriggerDto> triggers;
}
