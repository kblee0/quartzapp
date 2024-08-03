package com.home.quartzapp.scheduler.repository;

import java.util.List;
import java.util.Optional;

import com.home.quartzapp.scheduler.entity.JobHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JobHistoryRepository {
    Optional<JobHistory> getJobHistory(@Param("schedName") String schedName, @Param("entryId") String entryId);
    int insertJobHistory(JobHistory jobHistory);
    int updateJobHistory(JobHistory jobHistory);
    List<JobHistory> getJobHistoryList(@Param("jobGroup") String jobGroup, @Param("jobName") String jobName);
}
