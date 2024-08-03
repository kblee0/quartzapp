package com.home.quartzapp.scheduler.controller;

import com.home.quartzapp.common.exception.ApiException;
import com.home.quartzapp.scheduler.dto.JobDataMapDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;
import org.quartz.JobKey;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.home.quartzapp.scheduler.dto.JobInfoDto;
import com.home.quartzapp.scheduler.dto.JobListDto;
import com.home.quartzapp.scheduler.dto.JobStatusDto;
import com.home.quartzapp.scheduler.service.SchedulerService;

import java.net.URI;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1")
public class SchedulerController {
    private final SchedulerService schedulerService;

    @RequestMapping(value = "/scheduler/jobs", method = RequestMethod.POST)
    public ResponseEntity<?> addJob(@Valid @RequestBody JobInfoDto jobInfoDto) {
        JobKey jobKey = new JobKey(jobInfoDto.getName(), jobInfoDto.getGroup());
        JobStatusDto jobStatusDto;

        if (schedulerService.isJobExists(jobKey)) {
            throw ApiException.code("SCHE0001");
        }

        jobStatusDto = schedulerService.addJob(jobInfoDto);

        return ResponseEntity.created(URI.create("scheduler/jobs/" + jobKey.getGroup() + '/' + jobKey.getName())).body(jobStatusDto);
    }

    @RequestMapping(value = "/scheduler/jobs", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteJob(@RequestBody JobInfoDto jobInfoDto) {
        JobKey jobKey = new JobKey(jobInfoDto.getName(), jobInfoDto.getGroup());

        if (!schedulerService.isJobExists(jobKey)) {
            throw ApiException.code("SCHE0002");
        }

        schedulerService.deleteJob(jobInfoDto);

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/scheduler/jobs", method = RequestMethod.PUT)
    public ResponseEntity<?> updateJob(@RequestBody JobInfoDto jobInfoDto) {
        JobKey jobKey = new JobKey(jobInfoDto.getName(), jobInfoDto.getGroup());

        if (!schedulerService.isJobExists(jobKey)) {
            throw ApiException.code("SCHE0002");
        }

        JobStatusDto jobStatusDto;

        jobStatusDto = schedulerService.updateJob(jobInfoDto);

        return ResponseEntity.ok(jobStatusDto);
    }

    @RequestMapping(value = "/scheduler/jobs/{jobGroup}/{jobName}", method = RequestMethod.GET)
    public ResponseEntity<?> getJobStatus(
            @NotBlank @PathVariable(name = "jobGroup") String jobGroup,
            @NotBlank @PathVariable(name = "jobName") String jobName) {
        JobKey jobKey = new JobKey(jobName, jobGroup);

        if (!schedulerService.isJobExists(jobKey)) {
            throw ApiException.code("SCHE0002");
        }

        JobStatusDto jobStatusDto;

        jobStatusDto = schedulerService.getJobStatus(jobKey);

        return ResponseEntity.ok(jobStatusDto);
    }

    @RequestMapping(value = {"/scheduler/jobs", "/scheduler/jobs/{jobGroup}"}, method = RequestMethod.GET)
    public ResponseEntity<?> getJobList(
            @PathVariable(name = "jobGroup", required = false) String jobGroup) {

        JobListDto jobListDto;

        jobListDto = schedulerService.getJobList(jobGroup);

        return ResponseEntity.ok(jobListDto);
    }

    @RequestMapping(value = "/scheduler/jobs/{jobGroup}/{jobName}", method = RequestMethod.PATCH)
    public ResponseEntity<?> commandJob(
            @NotBlank @PathVariable(name = "jobGroup") String jobGroup,
            @NotBlank @PathVariable(name = "jobName") String jobName,
            @NotBlank @RequestParam(value = "command") String command,
            @RequestBody(required = false) JobDataMapDto jobDataMapDto) {
        JobKey jobKey = new JobKey(jobName, jobGroup);

        if (!schedulerService.isJobExists(jobKey)) {
            throw ApiException.code("SCHE0002");
        }

        JobStatusDto jobStatusDto = switch (command) {
            case "execute" -> schedulerService.executeJob(jobKey, jobDataMapDto);
            case "pause" -> schedulerService.pauseJob(jobKey);
            case "resume" -> schedulerService.resumeJob(jobKey);
            case "interrupt" -> schedulerService.interruptJob(jobKey);
            default -> throw ApiException.code("SCHE0007");
        };

        return ResponseEntity.ok(jobStatusDto);
    }
}
