package com.home.quartzapp.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor
@SuperBuilder
public class JobSimpleTriggerDto extends JobTriggerDto {
    private Integer repeatIntervalInSeconds;
    private Integer repeatCount;    // -1: Unlimited, 0: One-time (zero repeat)
    private Integer timesTriggered;
}
