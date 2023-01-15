package com.home.quartzapp.scheduler.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.home.quartzapp.scheduler.dto.ApiErrorDto;
import com.home.quartzapp.scheduler.dto.JobInfoDto;
import com.home.quartzapp.scheduler.dto.JobListDto;
import com.home.quartzapp.scheduler.dto.JobStatusDto;
import com.home.quartzapp.scheduler.exception.ErrorCode;
import com.home.quartzapp.scheduler.service.SchedulerService;

@Validated
@RestController
public class SchedulerController {
    @Autowired
    SchedulerService schedulerService;

    @RequestMapping(value = "/scheduler/jobs", method = RequestMethod.POST)
    public ResponseEntity<?> addJob(@Valid @RequestBody JobInfoDto jobInfoDto) {
        JobKey jobKey = new JobKey(jobInfoDto.getName(), jobInfoDto.getGroup());
        JobStatusDto jobStatusDto;

        if(schedulerService.isJobExists(jobKey)) {
            return ApiErrorDto.create(ErrorCode.JOB_ALREADY_EXIST);
        }

        jobStatusDto = schedulerService.addJob(jobInfoDto);

        return new ResponseEntity<>(
                jobStatusDto,
                HttpStatus.CREATED);
    }

    @RequestMapping(value = "/scheduler/jobs", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteJob(@RequestBody JobInfoDto jobInfoDto) {
        JobKey jobKey = new JobKey(jobInfoDto.getName(), jobInfoDto.getGroup());

        if(!schedulerService.isJobExists(jobKey)) {
            return ApiErrorDto.create(ErrorCode.JOB_DOES_NOT_EXIST);
        }

        schedulerService.deleteJob(jobInfoDto);

        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/scheduler/jobs", method = RequestMethod.PUT)
    public ResponseEntity<?> updateJob(@RequestBody JobInfoDto jobInfoDto) {
        JobKey jobKey = new JobKey(jobInfoDto.getName(), jobInfoDto.getGroup());

        if(!schedulerService.isJobExists(jobKey)) {
            return ApiErrorDto.create(ErrorCode.JOB_DOES_NOT_EXIST);
        }

        JobStatusDto jobStatusDto;

        jobStatusDto = schedulerService.updateJob(jobInfoDto);

        return new ResponseEntity<>(
            jobStatusDto,
            HttpStatus.OK
        );
    }

    @RequestMapping(value = "/scheduler/jobs/{jobGroup}/{jobName}", method = RequestMethod.GET)
    public ResponseEntity<?> getJobStatus(
        @NotBlank @PathVariable(name = "jobGroup") String jobGroup,
        @NotBlank @PathVariable(name = "jobName") String jobName) {
        JobKey jobKey = new JobKey(jobName, jobGroup);

        if(!schedulerService.isJobExists(jobKey)) {
            return ApiErrorDto.create(ErrorCode.JOB_DOES_NOT_EXIST);
        }
    
        JobStatusDto jobStatusDto;

        jobStatusDto = schedulerService.getJobStatus(jobKey);

        return new ResponseEntity<>(
            jobStatusDto,
            HttpStatus.OK);
    }

    @RequestMapping(value = {"/scheduler/jobs", "/scheduler/jobs/{jobGroup}"}, method = RequestMethod.GET)
    public ResponseEntity<?> getJobList(
        @PathVariable(name = "jobGroup", required = false) String jobGroup) {

        JobListDto jobListDto;

        jobListDto = schedulerService.getJobList(jobGroup);

        return new ResponseEntity<>(
            jobListDto,
            HttpStatus.OK);
    }

    @RequestMapping(value = "/scheduler/jobs/{jobGroup}/{jobName}/pause", method = RequestMethod.POST)
    public ResponseEntity<?> pauseJob(
        @NotBlank @PathVariable(name = "jobGroup") String jobGroup,
        @NotBlank @PathVariable(name = "jobName") String jobName) {
        JobKey jobKey = new JobKey(jobName, jobGroup);

        if(!schedulerService.isJobExists(jobKey)) {
            return ApiErrorDto.create(ErrorCode.JOB_DOES_NOT_EXIST);
        }
    
        JobStatusDto jobStatusDto;

        jobStatusDto = schedulerService.pauseJob(jobKey);

        return new ResponseEntity<>(
            jobStatusDto,
            HttpStatus.OK);
    }

    @RequestMapping(value = "/scheduler/jobs/{jobGroup}/{jobName}/resume", method = RequestMethod.POST)
    public ResponseEntity<?> resumeJob(
        @NotBlank @PathVariable(name = "jobGroup") String jobGroup,
        @NotBlank @PathVariable(name = "jobName") String jobName) {
        JobKey jobKey = new JobKey(jobName, jobGroup);

        if(!schedulerService.isJobExists(jobKey)) {
            return ApiErrorDto.create(ErrorCode.JOB_DOES_NOT_EXIST);
        }
    
        JobStatusDto jobStatusDto;

        jobStatusDto = schedulerService.resumeJob(jobKey);

        return new ResponseEntity<>(
            jobStatusDto,
            HttpStatus.OK);
    }

    @RequestMapping(value = "/scheduler/jobs/{jobGroup}/{jobName}/interrupt", method = RequestMethod.POST)
    public ResponseEntity<?> interruptJob(
        @NotBlank @PathVariable(name = "jobGroup") String jobGroup,
        @NotBlank @PathVariable(name = "jobName") String jobName) {
        JobKey jobKey = new JobKey(jobName, jobGroup);

        if(!schedulerService.isJobExists(jobKey)) {
            return ApiErrorDto.create(ErrorCode.JOB_DOES_NOT_EXIST);
        }
    
        JobStatusDto jobStatusDto;

        jobStatusDto = schedulerService.interruptJob(jobKey);

        return new ResponseEntity<>(
            jobStatusDto,
            HttpStatus.OK);
    }
}
