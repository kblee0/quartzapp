package com.home.quartzapp.batch.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@IdClass(BatchOutPk.class)
@Table(name = "T_BATCH_OUT")
public class BatchOut {
    @Id
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
