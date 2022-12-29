package com.home.quartzapp.scheduler.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.home.quartzapp.scheduler.entity.JobHistory;

@Mapper
public interface JobHistoryDAO {
    public JobHistory getJobHistory(@Param("schedName") String schedName, @Param("entryId") String entryId);
    public void insertJobHistory(JobHistory jobHistory);
    public void updateJobHistory(JobHistory jobHistory);
    public List<JobHistory> getJobHistoryList(@Param("jobGroup") String jobGroup, @Param("jobName") String jobName);
}
