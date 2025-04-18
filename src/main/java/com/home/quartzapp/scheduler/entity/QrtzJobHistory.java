package com.home.quartzapp.scheduler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@DynamicUpdate
@Table(name = "QRTZ_JOB_HISTORY")
public class QrtzJobHistory {
    @EmbeddedId
    private QrtzJobHistoryId id;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Size(max = 200)
    @NotNull
    @Column(name = "TRIGGER_NAME", nullable = false, length = 200)
    private String triggerName;

    @Size(max = 200)
    @NotNull
    @Column(name = "TRIGGER_GROUP", nullable = false, length = 200)
    private String triggerGroup;

    @Size(max = 4096)
    @Column(name = "JOB_DATA", length = 4096)
    private String jobData;

    @Size(max = 16)
    @NotNull
    @Column(name = "STATUS", nullable = false, length = 16)
    private String status;

    @Size(max = 2500)
    @Column(name = "EXIT_Code", length = 2500)
    private String exitCode;

    @Size(max = 2500)
    @Column(name = "EXIT_MESSAGE", length = 2500)
    private String exitMessage;

}