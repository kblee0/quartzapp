package com.home.quartzapp.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

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
    private Map<String, Object> jobDataMap;
    private Boolean interruptible;

    private Set<JobTriggerDto> triggers;
}
