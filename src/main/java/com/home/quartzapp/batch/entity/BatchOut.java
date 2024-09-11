package com.home.quartzapp.batch.entity;

import jakarta.persistence.Entity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BatchOut {
    LocalDateTime createDt;
    Long batchId;
    LocalDateTime startDt;
    LocalDateTime endDt;
    Long recCnt;
    Long outCnt;
}
