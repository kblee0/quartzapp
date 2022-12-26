package com.home.quartzapp.scheduler.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.home.quartzapp.scheduler.entity.JobHistory;

@Mapper
public interface JobHistoryDAO {
    public List<JobHistory> getAllJobHistory();
}
