package com.home.quartzapp.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class JobTriggerDto {
    @NotBlank
    private final String name;
    private String description;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // SimpleTrigger
    private Integer repeatIntervalInSeconds;
    private Integer repeatCount;    // -1: Unlimited, 0: One-time (zero repeat)
    private Integer timesTriggered;

    // CronTrigger
    private String cronExpression;

    private String state;
}
