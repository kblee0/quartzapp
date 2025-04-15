package com.home.quartzapp.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
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
    private String type;
    private String state;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String cronExpression;
    private Integer repeatIntervalInSeconds;
}
