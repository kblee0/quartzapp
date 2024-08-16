package com.home.quartzapp.scheduler.repository;

import com.home.quartzapp.scheduler.entity.QrtzJobHistory;
import com.home.quartzapp.scheduler.entity.QrtzJobHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QrtzJobHistoryRepository extends JpaRepository<QrtzJobHistory, QrtzJobHistoryId> {
    List<QrtzJobHistory> findByJobGroupAndJobName(String jobGroup, String jobName);

    @Modifying
    @Query("update QrtzJobHistory q set q.endTime = :endTime, q.status = :status, q.exitMessage = :exitMessage where q.id = :id")
    int updateEndTimeAndStatusAndExitMessageById(
            @Param("endTime") LocalDateTime endTime,
            @Param("status") String status,
            @Param("exitMessage") String exitMessage,
            @Param("id") QrtzJobHistoryId id);
}