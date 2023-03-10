package com.home.quartzapp.scheduler.service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.UnableToInterruptJobException;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.home.quartzapp.scheduler.dto.JobInfoDto;
import com.home.quartzapp.scheduler.dto.JobListDto;
import com.home.quartzapp.scheduler.dto.JobStatusDto;
import com.home.quartzapp.scheduler.exception.ApiException;
import com.home.quartzapp.scheduler.exception.ErrorCode;
import com.home.quartzapp.scheduler.util.DateTimeUtil;

import lombok.extern.slf4j.Slf4j;

import static org.quartz.CronExpression.isValidExpression;

@Slf4j
@Component
public class SchedulerService {
    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    // @Autowired
    // private ApplicationContext context;

    public JobStatusDto addJob(JobInfoDto jobInfoDto) {
        JobDetail jobDetail;
        Trigger trigger;

        try {
            jobDetail = this.createJobDetail(jobInfoDto);
            trigger = this.createTrigger(jobInfoDto);
            
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            Date dt = scheduler.scheduleJob(jobDetail, trigger);
            log.debug("Job with jobKey : {} scheduled successfully at date : {}", trigger.getJobKey(), dt);
            return getJobStatus(trigger.getJobKey());
        } catch (SchedulerException e) {
            log.error("error occurred while scheduling with jobInfoDto : {}", jobInfoDto, e);
            throw new ApiException(ErrorCode.JOB_SCHEDULER_EXCEPTION, e);
        }
    }

    public JobStatusDto updateJob(JobInfoDto jobInfoDto) {
        Trigger newTrigger;
        Scheduler scheduler;
        Date dt;

        newTrigger = this.createTrigger(jobInfoDto);

        scheduler = schedulerFactoryBean.getScheduler();

        try {
            JobDetail jobDetail = createJobDetail(jobInfoDto);

            // relpace job detail
            scheduler.addJob(jobDetail, true);

            if(scheduler.getTrigger(newTrigger.getKey()) != null) {
                dt = scheduler.rescheduleJob(newTrigger.getKey(), newTrigger);
            }
            else {
                dt = scheduler.scheduleJob(newTrigger);
            }
        } catch (SchedulerException e) {
            log.error("error occurred while scheduling with jobInfoDto : {}", jobInfoDto, e);
            throw new ApiException(ErrorCode.JOB_SCHEDULER_EXCEPTION, e);
        }
        log.debug("Job with jobInfoDto : {} rescheduled successfully at date : {}", jobInfoDto, dt);

        return getJobStatus(newTrigger.getJobKey());
    }


    public boolean deleteJob(JobInfoDto jobInfoDto) {
        JobKey jobKey = new JobKey(jobInfoDto.getName(), jobInfoDto.getGroup());
        log.debug("[schedulerdebug] deleting job with jobKey : {}", jobKey);
        try {
            boolean rv = schedulerFactoryBean.getScheduler().deleteJob(jobKey);
            return rv;
        } catch (SchedulerException e) {
            log.error("[schedulerdebug] error occurred while deleting job with jobKey : {}", jobKey, e);
            throw new ApiException(ErrorCode.JOB_SCHEDULER_EXCEPTION, e);
        }
    }

    /* Private Methods */
    private JobDetail createJobDetail(JobInfoDto jobInfoDto) {
        JobKey jobKey = new JobKey(jobInfoDto.getName(), jobInfoDto.getGroup());
        
        try {
            return JobBuilder.newJob()
                    .ofType(Class.forName(jobInfoDto.getJobClassName()).asSubclass(Job.class))
                    .storeDurably()
                    .withIdentity(jobKey)
                    .withDescription(jobInfoDto.getDescription())
                    .setJobData(jobInfoDto.getJobDataMap())
                    .build();
        } catch (ClassNotFoundException e) {
            throw new ApiException(ErrorCode.JOB_CLASS_NOT_FOUND, e);
        }
    }

    private Trigger createTrigger(JobInfoDto jobInfoDto) {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder
                                                    .newTrigger()
                                                    .forJob(jobInfoDto.getName(), jobInfoDto.getGroup())
                                                    .withIdentity(jobInfoDto.getName(), jobInfoDto.getGroup());

        if(jobInfoDto.getStartTime() != null) {
            Date startAt = Date.from(jobInfoDto.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
            triggerBuilder.startAt(startAt);
        }
        if(jobInfoDto.getEndTime() != null) {
            Date endAt = Date.from(jobInfoDto.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
            triggerBuilder.endAt(endAt);
        }

        String cronExpression = jobInfoDto.getCronExpression();

        if (jobInfoDto.isCronJob()) {
            if (!isValidExpression(cronExpression)) {
                throw new IllegalArgumentException("Provided expression " + cronExpression + " is not a valid cron expression");
            }
            // * CronTrigger misfire policy
            // - Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY ?????? ??????
            // - Trigger.MISFIRE_INSTRUCTION_SMART_POLICY ?????? misfire ????????? ????????????, ?????? misfire??? ?????? ?????????. fire now ??? ??????.
            // - CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW ?????? misfire ????????? ????????????, ?????? misfire??? ?????? ?????????.
            // - CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING ???????????? ??????.
            return triggerBuilder
                    .withSchedule(
                        CronScheduleBuilder
                            .cronSchedule(cronExpression)
                            // .withMisfireHandlingInstructionDoNothing()
                            )
                    .build();
        } else {
            // * SimpleTrigger misfire policy - limit count
            // - Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY ???????????? ????????? ???????????? job??? ?????????.
            // - Trigger.MISFIRE_INSTRUCTION_SMART_POLICY ???????????? ????????? ???????????? job??? ?????????. now existing(nothing) ??? ??????.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW ?????? misfire ????????? ????????????, ?????? misfire??? ?????? ?????????. now remaining ??? ??????.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT ???????????? ????????? ???????????? job??? ?????????. smart policy ??? ??????.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT ?????? misfire ????????? ????????????, ?????? misfire??? ?????? ?????????. fire now ??? ??????.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT ???????????? ?????? ??????. next remaining ??? ??????.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT ???????????? ?????? ??????.
            //
            // * SimpleTrigger misfire policy - unlimit count
            // - Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY ???????????? ????????? ???????????? job??? ?????????.
            // - Trigger.MISFIRE_INSTRUCTION_SMART_POLICY ???????????? ?????? ??????. next remaining ??? ??????.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW ?????? misfire ????????? ????????????, ?????? misfire??? ?????? ?????????. now remaining ??? ??????.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT ?????? misfire ????????? ????????????, ?????? misfire??? ?????? ?????????. now remaining ??? ??????.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT ?????? misfire ????????? ????????????, ?????? misfire??? ?????? ?????????.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT ???????????? ?????? ??????. next remaining ??? ??????.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT ???????????? ?????? ??????.
            return triggerBuilder
                    .withSchedule(
                        SimpleScheduleBuilder
                            .repeatSecondlyForever(jobInfoDto.getRepeatIntervalInSeconds())
                            .withRepeatCount(jobInfoDto.getRepeatCount())
                            // .withMisfireHandlingInstructionNextWithExistingCount()
                            )
                    .build();
        }
    }

    public JobStatusDto getJobStatus(JobKey jobKey) {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        try {
            JobDetail jobDetail;
            JobInfoDto jobInfoDto;
            JobStatusDto jobStatusDto;

            jobDetail = scheduler.getJobDetail(jobKey);

            jobInfoDto = JobInfoDto.builder()
                                    .group(jobDetail.getKey().getGroup())
                                    .name(jobDetail.getKey().getName())
                                    .description(jobDetail.getDescription())
                                    .jobClassName(jobDetail.getJobClass().getName())
                                    .jobDataMap(jobDetail.getJobDataMap())
                                    .build();

            TriggerKey triggerKey = new TriggerKey(jobKey.getName(), jobKey.getGroup());
            Trigger trigger = scheduler.getTrigger(triggerKey);

            if(trigger != null) {
                jobInfoDto.setStartTime(DateTimeUtil.toLocalDateTime(trigger.getStartTime()));
                jobInfoDto.setEndTime(DateTimeUtil.toLocalDateTime(trigger.getEndTime()));

                if(trigger instanceof CronTrigger) {
                    CronTrigger ct = (CronTrigger)trigger;
                    jobInfoDto.setCronExpression(ct.getCronExpression());
                }
                if(trigger instanceof SimpleTrigger) {
                    SimpleTrigger st = (SimpleTrigger)trigger;
                    jobInfoDto.setRepeatIntervalInSeconds((int)(st.getRepeatInterval()/1000));
                    jobInfoDto.setRepeatCount(st.getRepeatCount());
                }

                jobStatusDto = JobStatusDto.builder()
                            .jobInfoDto(jobInfoDto)
                            .lastFiredTime(DateTimeUtil.toLocalDateTime(trigger.getPreviousFireTime()))
                            .nextFireTime(DateTimeUtil.toLocalDateTime(trigger.getNextFireTime()))
                            .jobState(scheduler.getTriggerState(trigger.getKey()).name())
                            .build();
                if(isJobRunning(trigger.getJobKey())) {
                    jobStatusDto.setJobState("RUNNING");
                }
            }
            else {
                jobStatusDto = JobStatusDto.builder()
                            .jobInfoDto(jobInfoDto)
                            .jobState(scheduler.getTriggerState(triggerKey).name())
                            .build();
            }
            return jobStatusDto;
        } catch (SchedulerException e) {
            log.error("[schedulerdebug] error while fetching job info", e);
            throw new ApiException(ErrorCode.JOB_SCHEDULER_EXCEPTION, e);
        }
    }

    public JobListDto getJobList(String jobGroup) {
        JobStatusDto jobStatusDto;

        List<JobStatusDto> jobs = new ArrayList<>();

        int numOfRunningJobs = 0;
        int numOfGroups = 0;
        int numOfAllJobs = 0;

        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        try {
            List<String> groupList;

            groupList = StringUtils.hasText(jobGroup) ? Arrays.asList(jobGroup) : scheduler.getJobGroupNames();
            
            for(String groupName :  groupList) {
                numOfGroups++;
                for(JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    numOfAllJobs++;

                    jobStatusDto = getJobStatus(jobKey);
                    
                    jobs.add(jobStatusDto);  
                }
            }
        } catch (SchedulerException e) {
            log.error("[schedulerdebug] error while fetching all job info", e);
            throw new ApiException(ErrorCode.JOB_SCHEDULER_EXCEPTION, e);
        }

        return JobListDto.builder()
                .numOfAllJobs(numOfAllJobs)
                .numOfGroups(numOfGroups)
                .numOfRunningJobs(numOfRunningJobs)
                .jobs(jobs)
                .build();
    }

    public JobStatusDto pauseJob(JobKey jobKey) {
        try {
            schedulerFactoryBean.getScheduler().pauseJob(jobKey);
        } catch (SchedulerException e) {
            log.error("error occurred while pause job with jobKey : {}", jobKey, e);
            throw new ApiException(ErrorCode.JOB_SCHEDULER_EXCEPTION, e);
        }

        return getJobStatus(jobKey);
    }

    public JobStatusDto resumeJob(JobKey jobKey) {
        try {
            schedulerFactoryBean.getScheduler().resumeJob(jobKey);
        } catch (SchedulerException e) {
            log.error("error occurred while resume job with jobKey : {}", jobKey, e);
            throw new ApiException(ErrorCode.JOB_SCHEDULER_EXCEPTION, e);
        }

        return getJobStatus(jobKey);
    }

    public JobStatusDto interruptJob(JobKey jobKey) {
        try {
            schedulerFactoryBean.getScheduler().interrupt(jobKey);
        } catch (UnableToInterruptJobException e) {
            log.error("error occurred while interrupt job with jobKey : {}", jobKey, e);
            throw new ApiException(ErrorCode.JOB_SCHEDULER_EXCEPTION, e);
        }
        return getJobStatus(jobKey);
    }

    public boolean isJobExists(JobKey jobKey) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            if (scheduler.checkExists(jobKey)) {
                return true;
            }
        } catch (SchedulerException e) {
            log.error("[schedulerdebug] error occurred while checking job exists :: jobKey : {}", jobKey, e);
        }
        return false;
    }

    private boolean isJobRunning(JobKey jobKey) {
        try {
            List<JobExecutionContext> currentJobs = schedulerFactoryBean.getScheduler().getCurrentlyExecutingJobs();

            if(currentJobs != null) {
                for(JobExecutionContext jobCtx : currentJobs) {
                    if(jobKey.equals(jobCtx.getTrigger().getJobKey())) {
                        return true;
                    }
                }
            }
        } catch (SchedulerException e) {
            log.error("[schedulerdebug] error occurred while checking job with jobKey : {}", jobKey, e);
        }

        return false;
    }
}
