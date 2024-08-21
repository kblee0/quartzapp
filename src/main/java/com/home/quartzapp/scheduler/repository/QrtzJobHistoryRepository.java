package com.home.quartzapp.scheduler.repository;

import com.home.quartzapp.scheduler.entity.QrtzJobHistory;
import com.home.quartzapp.scheduler.entity.QrtzJobHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QrtzJobHistoryRepository extends JpaRepository<QrtzJobHistory, QrtzJobHistoryId> {
    List<QrtzJobHistory> findByJobGroupAndJobName(String jobGroup, String jobName);
}