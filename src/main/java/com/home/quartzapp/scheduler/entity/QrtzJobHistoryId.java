package com.home.quartzapp.scheduler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class QrtzJobHistoryId implements Serializable {
    @Size(max = 120)
    @NotNull
    @Column(name = "SCHED_NAME", nullable = false, length = 120)
    private String schedName;

    @Size(max = 200)
    @Column(name = "JOB_NAME", length = 200)
    private String jobName;

    @Size(max = 200)
    @Column(name = "JOB_GROUP", length = 200)
    private String jobGroup;

    @NotNull
    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        QrtzJobHistoryId entity = (QrtzJobHistoryId) o;
        return Objects.equals(this.schedName, entity.schedName) &&
                Objects.equals(this.jobName, entity.jobName) &&
                Objects.equals(this.jobGroup, entity.jobGroup) &&
                Objects.equals(this.startTime, entity.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedName, jobName, jobGroup, startTime);
    }

}