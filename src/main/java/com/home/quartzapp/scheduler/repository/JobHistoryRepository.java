package com.home.quartzapp.scheduler.repository;

import java.util.List;
import java.util.Optional;

import com.home.quartzapp.scheduler.entity.JobHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JobHistoryRepository {
    public Optional<JobHistory> getJobHistory(@Param("schedName") String schedName, @Param("entryId") String entryId);
    public int insertJobHistory(JobHistory jobHistory);
    public int updateJobHistory(JobHistory jobHistory);
    public List<JobHistory> getJobHistoryList(@Param("jobGroup") String jobGroup, @Param("jobName") String jobName);
}
