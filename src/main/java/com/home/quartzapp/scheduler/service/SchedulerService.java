package com.home.quartzapp.scheduler.service;

import java.time.ZoneId;
import java.util.*;

import com.home.quartzapp.common.exception.ApiException;
import com.home.quartzapp.scheduler.dto.*;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.home.quartzapp.common.util.DateTimeUtil;

import lombok.extern.slf4j.Slf4j;

import static org.quartz.CronExpression.isValidExpression;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
@ExecuteInJTATransaction
public class SchedulerService {
    private final SchedulerFactoryBean schedulerFactoryBean;

    public JobStatusDto addJob(JobInfoDto jobInfoDto) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = this.createJobDetail(jobInfoDto);

            Set<Trigger> triggers = new HashSet<>();
            if(jobInfoDto.getTriggers() != null)
                jobInfoDto.getTriggers().forEach(triggerDto -> triggers.add(this.createTrigger(jobDetail.getKey(), triggerDto)));

            scheduler.scheduleJob(jobDetail, triggers, false);
            log.debug("Job with jobKey : {} scheduled successfully.", jobDetail.getKey());
            return getJobStatus(jobDetail.getKey());
        } catch (SchedulerException e) {
            log.error("error occurred while scheduling with jobInfoDto : {}", jobInfoDto, e);
            throw ApiException.code("SCHE0004");
        }
    }

    public JobStatusDto updateJob(JobInfoDto jobInfoDto) {
        JobDetail jobDetail = this.createJobDetail(jobInfoDto);
        Scheduler scheduler = schedulerFactoryBean.getScheduler();


        Set<Trigger> triggers = new HashSet<>();
        jobInfoDto.getTriggers().forEach(t -> triggers.add(this.createTrigger(jobDetail.getKey(),t)));

        try {
            // JobDetail Update
            scheduler.addJob(jobDetail, true);

            Set<Trigger> currentTriggers = new HashSet<>(scheduler.getTriggersOfJob(jobDetail.getKey()));
            Trigger.TriggerState triggerState = Trigger.TriggerState.NONE;

            // Reschedule
            List<TriggerKey> delTriggers = new ArrayList<>();
            for(Trigger t : currentTriggers) {
                if(scheduler.getTriggerState(t.getKey()) == Trigger.TriggerState.PAUSED) {
                    triggerState = Trigger.TriggerState.PAUSED;
                }
                Trigger findTrigger = triggers.stream().filter(f -> f.getKey().equals(t.getKey())).findFirst().orElse(null);
                if(findTrigger == null) {
                    delTriggers.add(t.getKey());
                } else if(!findTrigger.getClass().equals(t.getClass())) {
                    scheduler.rescheduleJob(t.getKey(), findTrigger);
                    if(triggerState == Trigger.TriggerState.PAUSED) {
                        scheduler.pauseTrigger(findTrigger.getKey());
                    }
                }
            }
            // Unscheduled
            if(!delTriggers.isEmpty() ) scheduler.unscheduleJobs(delTriggers);

            // Add Schedule
            for(Trigger t : triggers) {
                Trigger findTrigger = currentTriggers.stream().filter(f -> f.getKey().equals(t.getKey())).findFirst().orElse(null);
                if(findTrigger == null) {
                    scheduler.scheduleJob(t);
                    if(triggerState == Trigger.TriggerState.PAUSED) {
                        scheduler.pauseTrigger(t.getKey());
                    }
                }
            }
            if(triggerState == Trigger.TriggerState.PAUSED) scheduler.pauseJob(jobDetail.getKey());
        } catch (SchedulerException e) {
            log.error("error occurred while scheduling with jobInfoDto : {}", jobInfoDto, e);
            throw ApiException.code("SCHE0004");
        }
        log.debug("Job with jobInfoDto : {} rescheduled successfully.", jobInfoDto);

        return getJobStatus(jobDetail.getKey());
    }


    public boolean deleteJob(JobInfoDto jobInfoDto) {
        JobKey jobKey = new JobKey(jobInfoDto.getName(), jobInfoDto.getGroup());
        log.debug("[schedulerdebug] deleting job with jobKey : {}", jobKey);
        try {
            return schedulerFactoryBean.getScheduler().deleteJob(jobKey);
        } catch (SchedulerException e) {
            log.error("[schedulerdebug] error occurred while deleting job with jobKey : {}", jobKey, e);
            throw ApiException.code("SCHE0004");
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
            throw ApiException.code("SCHE0003");
        }
    }

    private Trigger createTrigger(JobKey jobKey, JobTriggerDto jobTriggerDto) {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .forJob(jobKey)
                .withIdentity(jobTriggerDto.getName() ,jobKey.getGroup())
                .withDescription(jobTriggerDto.getDescription());

        if(jobTriggerDto.getStartTime() != null) {
            Date startAt = Date.from(jobTriggerDto.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
            triggerBuilder.startAt(startAt);
        }
        if(jobTriggerDto.getEndTime() != null) {
            Date endAt = Date.from(jobTriggerDto.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
            triggerBuilder.endAt(endAt);
        }

        String cronExpression = jobTriggerDto.getCronExpression();

        if (StringUtils.hasText(jobTriggerDto.getCronExpression())) {
            if (!isValidExpression(cronExpression)) {
                throw new IllegalArgumentException("Provided expression " + cronExpression + " is not a valid cron expression");
            }
            // * CronTrigger misfire policy
            // - Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY 모두 실행
            // - Trigger.MISFIRE_INSTRUCTION_SMART_POLICY 최초 misfire 된것은 실행하나, 이후 misfire된 것은 폐기됨. fire now 과 같음.
            // - CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW 최초 misfire 된것은 실행하나, 이후 misfire된 것은 폐기됨.
            // - CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING 아무것도 안함.
            return triggerBuilder
                    .withSchedule(
                        CronScheduleBuilder
                            .cronSchedule(cronExpression)
                            .withMisfireHandlingInstructionDoNothing()
                            )
                    .build();
        } else {
            // * SimpleTrigger misfire policy - limit count
            // - Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY 실행가능 상태가 되는대로 job을 실행함.
            // - Trigger.MISFIRE_INSTRUCTION_SMART_POLICY 실행가능 상태가 되는대로 job을 실행함. now existing(nothing) 과 같음.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW 최초 misfire 된것은 실행하나, 이후 misfire된 것은 폐기됨. now remaining 과 같음.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT 실행가능 상태가 되는대로 job을 실행함. smart policy 과 같음.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT 최초 misfire 된것은 실행하나, 이후 misfire된 것은 폐기됨. fire now 과 같음.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT 아무것도 하지 않음. next remaining 과 같음.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT 아무것도 하지 않음.
            //
            // * SimpleTrigger misfire policy - unlimit count
            // - Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY 실행가능 상태가 되는대로 job을 실행함.
            // - Trigger.MISFIRE_INSTRUCTION_SMART_POLICY 아무것도 하지 않음. next remaining 과 같음.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW 최초 misfire 된것은 실행하나, 이후 misfire된 것은 폐기됨. now remaining 과 같음.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT 최초 misfire 된것은 실행하나, 이후 misfire된 것은 폐기됨. now remaining 과 같음.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT 최초 misfire 된것은 실행하나, 이후 misfire된 것은 폐기됨.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT 아무것도 하지 않음. next remaining 과 같음.
            // - SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT 아무것도 하지 않음.
            return triggerBuilder
                    .withSchedule(
                        SimpleScheduleBuilder
                            .repeatSecondlyForever(jobTriggerDto.getRepeatIntervalInSeconds())
                            .withRepeatCount(Optional.ofNullable(jobTriggerDto.getRepeatCount()).orElse(-1))
                            .withMisfireHandlingInstructionNowWithRemainingCount()
                            )
                    .build();
        }
    }

    public JobStatusDto getJobStatus(JobKey jobKey) {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        try {
            JobDetail jobDetail;
            JobInfoDto jobInfoDto;
            JobStatusDto jobStatusDto = JobStatusDto.builder().build();

            jobDetail = scheduler.getJobDetail(jobKey);

            jobInfoDto = JobInfoDto.builder()
                    .group(jobDetail.getKey().getGroup())
                    .name(jobDetail.getKey().getName())
                    .description(jobDetail.getDescription())
                    .jobClassName(jobDetail.getJobClass().getName())
                    .interruptible(InterruptableJob.class.isAssignableFrom(jobDetail.getJobClass()))
                    .jobDataMap(jobDetail.getJobDataMap())
                    .triggers(new HashSet<>())
                    .build();

            for(Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
                JobTriggerDto triggerDto = JobTriggerDto.builder()
                        .name(trigger.getKey().getName())
                        .description(trigger.getDescription())
                        .startTime(DateTimeUtil.toLocalDateTime(trigger.getStartTime()))
                        .endTime(DateTimeUtil.toLocalDateTime(trigger.getEndTime()))
                        .state(scheduler.getTriggerState(trigger.getKey()).name())
                        .build();
                if (trigger instanceof CronTrigger ct) {
                    triggerDto.setCronExpression(ct.getCronExpression());
                }
                if (trigger instanceof SimpleTrigger st) {
                    triggerDto.setRepeatIntervalInSeconds((int) (st.getRepeatInterval() / 1000));
                    triggerDto.setRepeatCount(st.getRepeatCount());
                }

                if(jobStatusDto.getLastFiredTime() == null) {
                    jobStatusDto.setLastFiredTime(DateTimeUtil.toLocalDateTime(trigger.getPreviousFireTime()));
                } else {
                    if(trigger.getPreviousFireTime() != null) {
                        if(trigger.getPreviousFireTime().after(DateTimeUtil.toDate(jobStatusDto.getLastFiredTime()))) {
                            jobStatusDto.setLastFiredTime(DateTimeUtil.toLocalDateTime(trigger.getPreviousFireTime()));
                        }
                    }
                }
                if(jobStatusDto.getNextFireTime() == null) {
                    jobStatusDto.setNextFireTime(DateTimeUtil.toLocalDateTime(trigger.getNextFireTime()));
                } else {
                    if(trigger.getNextFireTime() != null) {
                        if(trigger.getNextFireTime().before(DateTimeUtil.toDate(jobStatusDto.getNextFireTime()))) {
                            jobStatusDto.setNextFireTime(DateTimeUtil.toLocalDateTime(trigger.getNextFireTime()));
                        }
                    }
                }

                jobStatusDto.setJobState(scheduler.getTriggerState(trigger.getKey()).name());

                jobInfoDto.getTriggers().add(triggerDto);
            }
            jobStatusDto.setJobInfoDto(jobInfoDto);

            if(isJobRunning(jobKey)) {
                jobStatusDto.setJobState("RUNNING");
            }
            return jobStatusDto;
        } catch (SchedulerException e) {
            log.error("[schedulerdebug] error while fetching job info", e);
            throw ApiException.code("SCHE0004");
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
            log.error("[schedulerdebug] error while fetching all job info", e);
            throw ApiException.code("SCHE0004");
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
            JobDataMap jobDataMap = Optional.ofNullable(jobDataMapDto).map(JobDataMapDto::getJobDataMap).orElse(null);
            if(jobDataMap == null) {
                schedulerFactoryBean.getScheduler().triggerJob(jobKey);
            }
            else {
                schedulerFactoryBean.getScheduler().triggerJob(jobKey, jobDataMap);
            }
        } catch (SchedulerException e) {
            log.error("error occurred while execute job with jobKey : {}", jobKey, e);
            throw ApiException.code("SCHE0004");
        }

        return getJobStatus(jobKey);
    }

    public JobStatusDto pauseJob(JobKey jobKey) {
        try {
            schedulerFactoryBean.getScheduler().pauseJob(jobKey);
        } catch (SchedulerException e) {
            log.error("error occurred while pause job with jobKey : {}", jobKey, e);
            throw ApiException.code("SCHE0004");
        }

        return getJobStatus(jobKey);
    }

    public JobStatusDto resumeJob(JobKey jobKey) {
        try {
            schedulerFactoryBean.getScheduler().resumeJob(jobKey);
        } catch (SchedulerException e) {
            log.error("error occurred while resume job with jobKey : {}", jobKey, e);
            throw ApiException.code("SCHE0004");
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
            throw ApiException.code("SCHE0004");
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
