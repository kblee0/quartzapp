package com.home.quartzapp.batch.repository;

import com.home.quartzapp.batch.entity.BatchIn;
import com.home.quartzapp.batch.entity.BatchOut;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BatchRepository {
    @Update(SQL_UPDATE_BATCH_IN_STATUS_BY_BATCH_ID)
    int updateBatchInStatusByBatchId(@Param("batchId") Long batchId, @Param("status") String status);

    @Insert(SQL_INSERT_BATCH_OUT)
    int insertBatchOut(@Param("item") BatchOut batchOut);

    @Select(SQL_SELECT_BATCH_IN_BY_PROCDATE_AND_STATUS)
    List<BatchIn> selectBatchInByProcDateAndStatus(@Param("procDate")String procDate, @Param("status")String status);

    String SQL_UPDATE_BATCH_IN_STATUS_BY_BATCH_ID = """
                    update T_BATCH_IN
                    set    STATUS = #{status}
                    where BATCH_ID = #{batchId}
            """;

    String SQL_INSERT_BATCH_OUT = """
                    insert into T_BATCH_OUT
                        (CREATE_DT, BATCH_ID, START_DT, END_DT, REC_CNT, OUT_CNT)
                    values
                        (#{item.createDt}, #{item.batchId}, #{item.startDt}, #{item.endDt}, #{item.recCnt}, #{item.outCnt})
            """;

    String SQL_SELECT_BATCH_IN_BY_PROCDATE_AND_STATUS = """
                    select BATCH_ID,
                        BATCH_NAME,
                        START_DT,
                        END_DT,
                        REC_CNT,
                        STATUS
                    from T_BATCH_IN
                    where BATCH_NAME = 'BAT001'
                    AND START_DT >= PARSEDATETIME (#{procDate}, 'yyyyMMdd')
                    AND START_DT <  PARSEDATETIME (#{procDate}, 'yyyyMMdd')+1
                    AND STATUS = #{status}
            """;
}
