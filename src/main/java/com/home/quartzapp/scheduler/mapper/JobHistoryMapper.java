package com.home.quartzapp.scheduler.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.home.quartzapp.scheduler.dto.JobHistoryDto;

@Mapper
public interface JobHistoryMapper {
    public JobHistoryDto getJobHistory(@Param("schedName") String schedName, @Param("entryId") String entryId);
    public void insertJobHistory(JobHistoryDto jobHistoryDto);
    public void updateJobHistory(JobHistoryDto jobHistoryDto);
    public List<JobHistoryDto> getJobHistoryList(@Param("jobGroup") String jobGroup, @Param("jobName") String jobName);
}
