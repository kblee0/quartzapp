package com.home.quartzapp.scheduler.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.home.quartzapp.scheduler.dto.JobHistoryDto;

@Mapper
public interface JobHistoryMapper {
    public Optional<JobHistoryDto> getJobHistory(@Param("schedName") String schedName, @Param("entryId") String entryId);
    public int insertJobHistory(JobHistoryDto jobHistoryDto);
    public int updateJobHistory(JobHistoryDto jobHistoryDto);
    public List<JobHistoryDto> getJobHistoryList(@Param("jobGroup") String jobGroup, @Param("jobName") String jobName);
}
