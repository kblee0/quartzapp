package com.home.quartzapp.scheduler.repository;

import com.home.quartzapp.scheduler.entity.QrtzJobHistory;
import com.home.quartzapp.scheduler.entity.QrtzJobHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QrtzJobHistoryRepository extends JpaRepository<QrtzJobHistory, QrtzJobHistoryId> {
    @Query("SELECT qh FROM QrtzJobHistory qh WHERE qh.id.jobGroup = :jobGroup AND qh.id.jobName = :jobName")
    List<QrtzJobHistory> findByJobGroupAndJobName(@Param("jobGroup")String jobGroup, @Param("jobName")String jobName);
}