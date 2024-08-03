package com.home.quartzapp.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JobTriggerDto {
    @NotBlank
    private String name;
    private String description;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // SimpleTrigger
    private Integer repeatIntervalInSeconds;
    private Integer repeatCount;    // -1: Unlimited, 0: One-time (zero repeat)

    // CronTrigger
    private String cronExpression;

    private String state;
}
