package com.home.quartzapp.scheduler.controller;

import com.home.quartzapp.common.exception.ApiException;
import com.home.quartzapp.scheduler.dto.JobDataMapDto;
import com.home.quartzapp.scheduler.dto.JobInfoDto;
import com.home.quartzapp.scheduler.dto.JobStatusDto;
import com.home.quartzapp.scheduler.service.SchedulerService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.quartz.JobKey;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1")
@Tag(name="Quartz job management API", description="API for Quartz job management")
public class SchedulerController {
    private final SchedulerService schedulerService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/scheduler/job", method = RequestMethod.POST)
    public ResponseEntity<?> addJob(@Valid @RequestBody JobInfoDto jobInfoDto) {
        JobKey jobKey = new JobKey(jobInfoDto.getName(), jobInfoDto.getGroup());
        JobStatusDto jobStatusDto;

        if (schedulerService.isJobExists(jobKey)) {
            throw ApiException.code("SCHE0001");
        }

        jobStatusDto = schedulerService.addJob(jobInfoDto);

        return ResponseEntity.created(URI.create("scheduler/jobs/" + jobKey.getGroup() + '/' + jobKey.getName())).body(jobStatusDto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/scheduler/job", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteJob(@RequestBody JobInfoDto jobInfoDto) {
        JobKey jobKey = new JobKey(jobInfoDto.getName(), jobInfoDto.getGroup());

        if (!schedulerService.isJobExists(jobKey)) {
            throw ApiException.code("SCHE0002");
        }

        schedulerService.deleteJob(jobInfoDto);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/scheduler/job", method = RequestMethod.PUT)
    public ResponseEntity<?> updateJob(@RequestBody JobInfoDto jobInfoDto) {
        JobKey jobKey = new JobKey(jobInfoDto.getName(), jobInfoDto.getGroup());

        if (!schedulerService.isJobExists(jobKey)) throw ApiException.code("SCHE0002");

        return ResponseEntity.ok(schedulerService.updateJob(jobInfoDto));
    }

    @RequestMapping(value = "/scheduler/job", method = RequestMethod.GET)
    public ResponseEntity<?> getJobStatus(
            @RequestParam(name = "group", required = false) String jobGroup,
            @RequestParam(name = "name", required = false) String jobName) {
        if(StringUtils.hasText(jobName)) {
            JobKey jobKey = new JobKey(jobName, jobGroup);
            if (!schedulerService.isJobExists(jobKey)) throw ApiException.code("SCHE0002");

            return ResponseEntity.ok(schedulerService.getJobStatus(jobKey));
        }
        else {
            return ResponseEntity.ok(schedulerService.getJobList(jobGroup));
        }
    }

    @RequestMapping(value = "/scheduler/job", method = RequestMethod.PATCH)
    public ResponseEntity<?> commandJob(
            @RequestParam(name = "group", required = false) String jobGroup,
            @RequestParam(name = "name") String jobName,
            @Parameter(schema = @Schema(allowableValues = {"execute", "pause", "resume", "interrupt", "recover"})) @RequestParam(value = "command") String command
            ) {
        JobKey jobKey = new JobKey(jobName, jobGroup);

        if (!schedulerService.isJobExists(jobKey)) throw ApiException.code("SCHE0002");

        JobStatusDto jobStatusDto = switch (command) {
            case "execute" -> schedulerService.executeJob(jobKey);
            case "pause" -> schedulerService.pauseJob(jobKey);
            case "resume" -> schedulerService.resumeJob(jobKey);
            case "interrupt" -> schedulerService.interruptJob(jobKey);
            case "recover" -> schedulerService.recoverJob(jobKey);
            default -> throw ApiException.code("SCHE0007");
        };
        return ResponseEntity.ok(jobStatusDto);
    }
}
