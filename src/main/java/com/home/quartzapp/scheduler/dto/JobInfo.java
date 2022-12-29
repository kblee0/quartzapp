package com.home.quartzapp.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.quartz.JobDataMap;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobInfo {
    // JobDetail
    @NotBlank(message = "Group name cannot be empty...")
    private String group;
    @NotBlank
    private String name;
    private String description;
    private String jobClassName;
    private JobDataMap jobDataMap;

    // Trigger
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    // SimpleTrigger
    private int repeatIntervalInSeconds;
    private int repeatCount;    // -1: Unlimit, 0: One-time (zero repeat)

    // CronTrigger
    private String cronExpression;

    public boolean isCronJob() {
        return StringUtils.hasText(this.cronExpression);
    }
}
