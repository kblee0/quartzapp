package com.home.quartzapp.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BatchOutPk implements Serializable {
    LocalDateTime createDt;
    private Long batchId;
}
