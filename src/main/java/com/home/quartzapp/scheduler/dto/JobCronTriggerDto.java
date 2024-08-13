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
public class JobCronTriggerDto extends JobTriggerDto {
    private String cronExpression;
}
