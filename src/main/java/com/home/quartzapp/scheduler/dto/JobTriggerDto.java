package com.home.quartzapp.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.quartz.impl.jdbcjobstore.Constants;

import java.time.LocalDateTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = JobCronTriggerDto.class, name = Constants.TTYPE_CRON),
    @JsonSubTypes.Type(value = JobSimpleTriggerDto.class, name = Constants.TTYPE_SIMPLE)
})
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@SuperBuilder
public abstract class JobTriggerDto {
    @JsonIgnore
    private String group;
    @NotBlank
    private final String name;
    private String description;
    private String type;
    private String state;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
