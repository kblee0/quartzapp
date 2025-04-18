package com.home.quartzapp.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
public class JobTriggerDto {
    @JsonIgnore
    private String group;
    @NotBlank
    private final String name;
    private String description;
    @Pattern(regexp = "^(CRON|SIMPLE|FIXED|ONCE)$", message = "The trigger type can only be set to CRON, SIMPLE, FIXED, and ONCE.")
    private String type;
    private String state;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String cronExpression;
    private Integer repeatIntervalInSeconds;
}
