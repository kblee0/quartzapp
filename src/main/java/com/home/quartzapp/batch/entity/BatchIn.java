package com.home.quartzapp.batch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchIn {
    private Long batchId;
    private String batchName;
    private LocalDateTime startDt;
    private LocalDateTime endDt;
    private Long recCnt;
    private String status;
}
