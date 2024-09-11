package com.home.quartzapp.batch.repository;

import com.home.quartzapp.batch.entity.BatchOut;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BatchRepository {
    int updateBatchInStatusByBatchId(@Param("batchId") Long batchId, @Param("status") String status);
    int insertBatchOut(@Param("item") BatchOut batchOut);
}
