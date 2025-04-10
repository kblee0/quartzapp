package com.home.quartzapp.scheduler.service;

import com.home.quartzapp.common.exception.ApiException;
import com.home.quartzapp.common.util.DateTimeUtil;
import com.home.quartzapp.scheduler.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.jdbcjobstore.Constants;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class SchedulerService {
    private final SchedulerFactoryBean schedulerFactoryBean;

    private String getTriggerGroup(JobKey jobKey) {
        return jobKey.getGroup() + "." + jobKey.getName();
    }

    public JobStatusDto addJob(JobInfoDto jobInfoDto) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = this.createJobDetail(jobInfoDto);

            Set<Trigger> triggers = new HashSet<>();
            if(jobInfoDto.getTriggers() != null)
                jobInfoDto.getTriggers().forEach(triggerDto -> triggers.add(this.createTrigger(jobDetail.getKey(), triggerDto, null)));

            scheduler.scheduleJob(jobDetail, triggers, false);
            log.debug("Job with jobKey : {} scheduled successfully.", jobDetail.getKey());
            return getJobStatus(jobDetail.getKey());
        } catch (SchedulerException e) {
            log.error("error occurred while scheduling with jobInfoDto : {}", jobInfoDto, e);
            throw ApiException.code("SCHE0004", e, e.getMessage());
        }
    }

    public JobStatusDto updateJob(JobInfoDto jobInfoDto) {
        JobDetail jobDetail = this.createJobDetail(jobInfoDto);
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        Set<Trigger> triggers = new HashSet<>();

        if(jobInfoDto.getTriggers()!=null) jobInfoDto.getTriggers().forEach(t -> triggers.add(this.createTrigger(jobDetail.getKey(),t,null)));

        try {
            // JobDetail Update
            scheduler.addJob(jobDetail, true);

            Set<Trigger> currentTriggers = new HashSet<>(scheduler.getTriggersOfJob(jobDetail.getKey()));
            HashSet<Trigger.TriggerState> triggerStates = new HashSet<>();

            // Reschedule
            List<TriggerKey> delTriggers = new ArrayList<>();
            for(Trigger t : currentTriggers) {
                triggerStates.add(scheduler.getTriggerState(t.getKey()));

                Trigger findTrigger = triggers.stream().filter(f -> f.getKey().equals(t.getKey())).findFirst().orElse(null);
                // Trigger for deletion if not in the new list or of a different type
                if(findTrigger == null || !findTrigger.getClass().equals(t.getClass())) {
                    delTriggers.add(t.getKey());
                } else {
                    // Triggers of the same type change schedule
                    scheduler.rescheduleJob(t.getKey(), findTrigger);
                    // Change the state if any of the existing triggers are in the PAUSED state.
                    if(triggerStates.contains(Trigger.TriggerState.PAUSED)) {
                        scheduler.pauseTrigger(findTrigger.getKey());
                    }
                }
            }
            // Delete target trigger
            if(!delTriggers.isEmpty() ) scheduler.unscheduleJobs(delTriggers);

            // Add new trigger
            for(Trigger t : triggers) {
                Trigger findTrigger = currentTriggers.stream().filter(f -> f.getKey().equals(t.getKey())).findFirst().orElse(null);
                if(findTrigger == null) {
                    scheduler.scheduleJob(t);
                    // Change the state if any of the existing triggers are in the PAUSED state.
                    if(triggerStates.contains(Trigger.TriggerState.PAUSED)) {
                        scheduler.pauseTrigger(t.getKey());
                    }
                }
            }
            // Change job status to prevent missing status changes
            if(triggerStates.contains(Trigger.TriggerState.PAUSED)) scheduler.pauseJob(jobDetail.getKey());
        } catch (SchedulerException e) {
            log.error("error occurred while scheduling with jobInfoDto : {}", jobInfoDto);
            throw ApiException.code("SCHE0004", e, "Job update failed.");
        }
        log.debug("Job with jobInfoDto : {} rescheduled successfully.", jobInfoDto);

        return getJobStatus(jobDetail.getKey());
    }


    public boolean deleteJob(JobInfoDto jobInfoDto) {
        JobKey jobKey = new JobKey(jobInfoDto.getName(), jobInfoDto.getGroup());
        log.debug("deleting job with jobKey : {}", jobKey);
        try {
            return schedulerFactoryBean.getScheduler().deleteJob(jobKey);
        } catch (SchedulerException e) {
            log.error("Error occurred while deleting job with jobKey : {}", jobKey);
            throw ApiException.code("SCHE0004", e, "Job delete failed.");
        }
    }

    /* Private Methods */
    private JobDetail createJobDetail(JobInfoDto jobInfoDto) {
        JobKey jobKey = new JobKey(jobInfoDto.getName(), jobInfoDto.getGroup());

        if(jobInfoDto.getJobDataMap() == null) {
            jobInfoDto.setJobDataMap(new JobDataMap());
            jobInfoDto.getJobDataMap().put("jobName", jobKey.getName());
        }
        try {
            if(!Job.class.isAssignableFrom(Class.forName(jobInfoDto.getJobClassName()))) {
                throw ApiException.code("SCHE0006");
            }
            return JobBuilder.newJob()
                    .ofType(Class.forName(jobInfoDto.getJobClassName()).asSubclass(Job.class))
                    .storeDurably()
                    .withIdentity(jobKey)
                    .withDescription(jobInfoDto.getDescription())
                    .setJobData(jobInfoDto.getJobDataMap())
                    .build();
        } catch (ClassNotFoundException e) {
            throw ApiException.code("SCHE0003", e);
        }
    }

    private Trigger createTrigger(JobKey jobKey, JobTriggerDto jobTriggerDto, JobDataMap jobDataMap) {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .forJob(jobKey)
                .withIdentity(jobTriggerDto.getName(), this.getTriggerGroup(jobKey))
                .withDescription(jobTriggerDto.getDescription());

        if(jobDataMap == null) {
            jobDataMap = new JobDataMap();
        }
//        if(jobDataMap != null) {
//            triggerBuilder.usingJobData(jobDataMap);
//        }
        if(jobTriggerDto.getStartTime() != null) {
            triggerBuilder.startAt(DateTimeUtil.toDate(jobTriggerDto.getStartTime()));
        }
        if(jobTriggerDto.getEndTime() != null) {
            triggerBuilder.endAt(DateTimeUtil.toDate(jobTriggerDto.getEndTime()));
        }

        if(jobTriggerDto instanceof JobCronTriggerDto jobCronTriggerDto) {
            if (!CronExpression.isValidExpression(jobCronTriggerDto.getCronExpression())) {
                throw new IllegalArgumentException("Provided expression " + jobCronTriggerDto.getCronExpression() + " is not a valid cron expression");
            }
            // Misfire : 무시
            jobDataMap.put("TriggerType", Constants.TTYPE_CRON);
            return triggerBuilder
                    .usingJobData(jobDataMap)
                    .withSchedule(
                        CronScheduleBuilder
                                .cronSchedule(jobCronTriggerDto.getCronExpression())
                                .withMisfireHandlingInstructionDoNothing()
                    ).build();
        } else {
            JobSimpleTriggerDto jobSimpleTriggerDto = (JobSimpleTriggerDto) jobTriggerDto;

            // Misfire : 무시
            jobDataMap.put("TriggerType", Constants.TTYPE_SIMPLE);
            return triggerBuilder
                    .usingJobData(jobDataMap)
                    .withSchedule(
                        SimpleScheduleBuilder
                                .repeatSecondlyForever(jobSimpleTriggerDto.getRepeatIntervalInSeconds())
                                .withRepeatCount(Optional.ofNullable(jobSimpleTriggerDto.getRepeatCount()).orElse(-1))
                                .withMisfireHandlingInstructionNextWithRemainingCount()
                    ).build();
        }
    }

    public JobStatusDto getJobStatus(JobKey jobKey) {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        HashSet<Trigger.TriggerState> jobTriggerStates = new HashSet<>();

        try {
            JobDetail jobDetail;
            JobInfoDto jobInfoDto;
            JobStatusDto jobStatusDto = JobStatusDto.builder().build();

            jobDetail = scheduler.getJobDetail(jobKey);

            jobInfoDto = JobInfoDto.builder()
                    .group(jobDetail.getKey().getGroup())
                    .name(jobDetail.getKey().getName())
                    .description(jobDetail.getDescription())
                    .jobClassName(jobDetail.getJobClass().equals(Job.class) ? "Unknown Class" : jobDetail.getJobClass().getName())
                    .interruptible(InterruptableJob.class.isAssignableFrom(jobDetail.getJobClass()))
                    .jobDataMap(jobDetail.getJobDataMap())
                    .triggers(new HashSet<>())
                    .build();

            for(Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
                jobStatusDto.setLastFiredTime(DateTimeUtil.max(jobStatusDto.getLastFiredTime(), DateTimeUtil.toLocalDateTime(trigger.getPreviousFireTime())));
                jobStatusDto.setNextFireTime(DateTimeUtil.min(jobStatusDto.getNextFireTime(), DateTimeUtil.toLocalDateTime(trigger.getNextFireTime())));

                jobTriggerStates.add(scheduler.getTriggerState(trigger.getKey()));

                jobInfoDto.getTriggers().add(this.createJobTriggerDto(trigger));
            }
            jobStatusDto.setJobInfoDto(jobInfoDto);

            if(jobTriggerStates.isEmpty()) jobStatusDto.setJobState(Trigger.TriggerState.NONE.name());
            else if(jobTriggerStates.contains(Trigger.TriggerState.ERROR)) jobStatusDto.setJobState(Trigger.TriggerState.ERROR.name());
            else if(jobTriggerStates.contains(Trigger.TriggerState.BLOCKED)) jobStatusDto.setJobState(Trigger.TriggerState.BLOCKED.name());
            else if(jobTriggerStates.contains(Trigger.TriggerState.PAUSED)) jobStatusDto.setJobState(Trigger.TriggerState.PAUSED.name());
            else if(jobTriggerStates.contains(Trigger.TriggerState.NORMAL)) jobStatusDto.setJobState(Trigger.TriggerState.NORMAL.name());
            else jobStatusDto.setJobState(Trigger.TriggerState.NONE.name());

            if(isJobRunning(jobKey)) jobStatusDto.setJobState("RUNNING");

            return jobStatusDto;
        } catch (SchedulerException e) {
            log.error("Error while fetching job info", e);
            throw ApiException.code("SCHE0004", e, "Error while fetching job info");
        }
    }

    private JobTriggerDto createJobTriggerDto(Trigger trigger) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        if (trigger instanceof CronTrigger cronTrigger) {
            return JobCronTriggerDto.builder()
                    .type(Constants.TTYPE_CRON)
                    .group(trigger.getKey().getGroup())
                    .name(trigger.getKey().getName())
                    .description(trigger.getDescription())
                    .startTime(DateTimeUtil.toLocalDateTime(trigger.getStartTime()))
                    .endTime(DateTimeUtil.toLocalDateTime(trigger.getEndTime()))
                    .state(scheduler.getTriggerState(trigger.getKey()).name())
                    .cronExpression(cronTrigger.getCronExpression())
                    .build();
        }
        else {
            SimpleTrigger simpleTrigger = (SimpleTrigger)trigger;

            return JobSimpleTriggerDto.builder()
                    .type(Constants.TTYPE_SIMPLE)
                    .group(trigger.getKey().getGroup())
                    .name(trigger.getKey().getName())
                    .description(trigger.getDescription())
                    .startTime(DateTimeUtil.toLocalDateTime(trigger.getStartTime()))
                    .endTime(DateTimeUtil.toLocalDateTime(trigger.getEndTime()))
                    .state(scheduler.getTriggerState(trigger.getKey()).name())
                    .repeatIntervalInSeconds((int) (simpleTrigger.getRepeatInterval() / 1000))
                    .repeatCount(simpleTrigger.getRepeatCount())
                    .timesTriggered(simpleTrigger.getTimesTriggered())
                    .build();
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

            groupList = StringUtils.hasText(jobGroup) ? List.of(jobGroup) : scheduler.getJobGroupNames();
            
            for(String groupName :  groupList) {
                numOfGroups++;
                for(JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    numOfAllJobs++;

                    jobStatusDto = getJobStatus(jobKey);
                    
                    jobs.add(jobStatusDto);  
                }
            }
        } catch (SchedulerException e) {
            log.error("Error while fetching all job info", e);
            throw ApiException.code("SCHE0004", e, "Error while fetching all job info");
        }

        return JobListDto.builder()
                .numOfAllJobs(numOfAllJobs)
                .numOfGroups(numOfGroups)
                .numOfRunningJobs(numOfRunningJobs)
                .jobs(jobs)
                .build();
    }

    public JobStatusDto executeJob(JobKey jobKey, JobDataMapDto jobDataMapDto) {
        try {
            JobSimpleTriggerDto jobTriggerDto = JobSimpleTriggerDto.builder()
                    .type(Constants.TTYPE_SIMPLE)
                    .group(this.getTriggerGroup(jobKey))
                    .name("@Once-"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss:SSS")))
                    .repeatIntervalInSeconds(0)
                    .repeatCount(0)
                    .build();
            SimpleTrigger trigger = (SimpleTrigger)createTrigger(jobKey, jobTriggerDto, jobDataMapDto != null ? jobDataMapDto.getJobDataMap() : null);

            schedulerFactoryBean.getScheduler().scheduleJob(trigger);
        } catch (SchedulerException e) {
            log.error("error occurred while execute job with jobKey : {}", jobKey, e);
            throw ApiException.code("SCHE0004", e, "Error while execute job");
        }

        return getJobStatus(jobKey);
    }

    public JobStatusDto pauseJob(JobKey jobKey) {
        try {
            schedulerFactoryBean.getScheduler().pauseJob(jobKey);
        } catch (SchedulerException e) {
            log.error("error occurred while pause job with jobKey : {}", jobKey, e);
            throw ApiException.code("SCHE0004", e, "Error while pause job");
        }

        return getJobStatus(jobKey);
    }

    public JobStatusDto resumeJob(JobKey jobKey) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            log.error("error occurred while resume job with jobKey : {}", jobKey, e);
            throw ApiException.code("SCHE0004", e, "Error while resume job");
        }
        return getJobStatus(jobKey);
    }

    public JobStatusDto recoverJob(JobKey jobKey) {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        try {
            for(Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
                if(Trigger.TriggerState.ERROR.equals(scheduler.getTriggerState(trigger.getKey()))){
                    if(trigger instanceof SimpleTrigger simpleTrigger) {
                        if(simpleTrigger.getRepeatCount() == 0 && simpleTrigger.getTimesTriggered() > 0) {
                            log.warn("Delete a trigger that is executed only once and has an error status :: JobGroup: {}, JobName: {}, TriggerName: {}",
                                    trigger.getJobKey().getGroup(), trigger.getJobKey().getName(), trigger.getKey().getName());
                            scheduler.unscheduleJob(trigger.getKey());
                            continue;
                        }
                    }
                    scheduler.resetTriggerFromErrorState(trigger.getKey());
                }
            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        return getJobStatus(jobKey);
    }

    public JobStatusDto interruptJob(JobKey jobKey) {
        JobDetail jobDetail;

        try {
            jobDetail = schedulerFactoryBean.getScheduler().getJobDetail(jobKey);

            if(!jobDetail.getClass().isAssignableFrom(InterruptableJob.class)) {
                throw ApiException.code("SCHE0005");
            }
            schedulerFactoryBean.getScheduler().interrupt(jobKey);
        } catch (SchedulerException e) {
            log.error("error occurred while interrupt job with jobKey : {}", jobKey, e);
            throw ApiException.code("SCHE0004", e, "Error while interrupt job");
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
            log.error("Error occurred while checking job exists :: jobKey : {}", jobKey, e);
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
            log.error("Error occurred while checking job with jobKey : {}", jobKey, e);
        }

        return false;
    }
}
