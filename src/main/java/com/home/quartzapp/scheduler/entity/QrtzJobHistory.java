package com.home.quartzapp.scheduler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "QRTZ_JOB_HISTORY")
public class QrtzJobHistory {
    @EmbeddedId
    private QrtzJobHistoryId id;

    @Size(max = 190)
    @NotNull
    @Column(name = "TRIGGER_NAME", nullable = false, length = 190)
    private String triggerName;

    @Size(max = 190)
    @NotNull
    @Column(name = "TRIGGER_GROUP", nullable = false, length = 190)
    private String triggerGroup;

    @Size(max = 190)
    @Column(name = "JOB_NAME", length = 190)
    private String jobName;

    @Size(max = 190)
    @Column(name = "JOB_GROUP", length = 190)
    private String jobGroup;

    @Size(max = 4096)
    @Column(name = "JOB_DATA", length = 4096)
    private String jobData;

    @NotNull
    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Size(max = 16)
    @NotNull
    @Column(name = "STATUS", nullable = false, length = 16)
    private String status;

    @Size(max = 2500)
    @Column(name = "EXIT_MESSAGE", length = 2500)
    private String exitMessage;

}