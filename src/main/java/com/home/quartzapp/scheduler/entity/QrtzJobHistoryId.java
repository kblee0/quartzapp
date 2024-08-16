package com.home.quartzapp.scheduler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serializable;
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

    @Size(max = 95)
    @NotNull
    @Column(name = "ENTRY_ID", nullable = false, length = 95)
    private String entryId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        QrtzJobHistoryId entity = (QrtzJobHistoryId) o;
        return Objects.equals(this.schedName, entity.schedName) &&
                Objects.equals(this.entryId, entity.entryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedName, entryId);
    }

}