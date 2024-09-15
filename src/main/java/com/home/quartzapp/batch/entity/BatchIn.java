package com.home.quartzapp.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "T_BATCH_IN")
public class BatchIn {
    @Id
    @Column(name = "BATCH_ID")
    private Long batchId;

    @Size(max = 255)
    @Column(name = "BATCH_NAME")
    private String batchName;

    @Column(name = "START_DT")
    private LocalDateTime startDt;

    @Column(name = "END_DT")
    private LocalDateTime endDt;

    @Column(name = "REC_CNT")
    private Long recCnt;

    @Size(max = 2)
    @Column(name = "STATUS", length = 2)
    private String status;

}
