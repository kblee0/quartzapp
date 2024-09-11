package com.home.quartzapp.batch.service;

import com.home.quartzapp.batch.entity.BatchIn;
import com.home.quartzapp.batch.entity.BatchOut;
import com.home.quartzapp.batch.repository.BatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BatchService {
    private final BatchRepository batchRepository;

    public void createBatchOut(BatchIn batchIn) {
        BatchOut batchOut = new BatchOut();
        batchOut.setBatchId(batchIn.getBatchId());
        batchOut.setCreateDt(LocalDateTime.now());
        batchOut.setStartDt(batchIn.getStartDt());
        batchOut.setEndDt(batchIn.getEndDt());
        batchOut.setRecCnt(batchIn.getRecCnt());
        batchOut.setOutCnt(batchIn.getRecCnt());
        if(batchIn.getBatchId() == 59) {
            throw new RuntimeException("Batch Id error > 60");
        }
        batchRepository.insertBatchOut(batchOut);
        batchRepository.updateBatchInStatusByBatchId(batchIn.getBatchId(), "CO");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void forceUpdateBatchInStatus(Long batchId, String status) {
        batchRepository.updateBatchInStatusByBatchId(batchId, status);
    }
}
