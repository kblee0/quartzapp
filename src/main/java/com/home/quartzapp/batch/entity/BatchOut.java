package com.home.quartzapp.batch.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "T_BATCH_OUT", uniqueConstraints = {@UniqueConstraint(columnNames = {"CREATE_DT", "BATCH_ID"})})
public class BatchOut {
    @Column(name = "CREATE_DT")
    LocalDateTime createDt;

    @Id
    @Column(name = "BATCH_ID")
    private Long batchId;

    @Column(name = "START_DT")
    private LocalDateTime startDt;

    @Column(name = "END_DT")
    private LocalDateTime endDt;

    @Column(name = "REC_CNT")
    private Long recCnt;

    @Column(name = "OUT_CNT")
    private Long outCnt;

}
