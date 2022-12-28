package com.home.quartzapp.scheduler.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.home.quartzapp.scheduler.entity.JobHistory;

@Mapper
public interface JobHistoryDAO {
    public JobHistory getJobHistory(String schedName, String entryId);
    public void createJobHistory(JobHistory jobHistory);
    public void updateJobHistory(JobHistory jobHistory);
    public List<JobHistory> getJobHistoryList(String jobGroup, String jobName);
}
