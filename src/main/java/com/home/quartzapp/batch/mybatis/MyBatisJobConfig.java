package com.home.quartzapp.batch.mybatis;

import com.home.quartzapp.batch.entity.BatchIn;
import com.home.quartzapp.batch.entity.BatchOut;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.mybatis.spring.batch.builder.MyBatisCursorItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MyBatisJobConfig {
    private final SqlSessionFactory sqlSessionFactory;

    @Bean
    public Job myBatisJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new JobBuilder("myBatisJob", jobRepository)
                .start(myBatisStep(jobRepository, platformTransactionManager))
                .build();
    }

    @Bean
    public Step myBatisStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("myBatisStep", jobRepository)
                .<BatchIn,BatchOut>chunk(3, platformTransactionManager)
                .reader(myBatisItemReader())
                .processor(myBatisItemProcessor())
                .writer(myBatisItemWriter())
                .build();
    }

    @Bean
    public MyBatisCursorItemReader<BatchIn> myBatisItemReader() {
        Map<String, Object> params = new HashMap<>() {{
            put("procDate", "20240903");
            put("status", "RD");
        }};

        return new MyBatisCursorItemReaderBuilder<BatchIn>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("com.home.quartzapp.batch.repository.BatchRepository.selectBatchInByProcDateAndStatus")
                .parameterValues(params)
                .build();
    }

    @Bean
    public ItemProcessor<BatchIn, BatchOut> myBatisItemProcessor() {
        return item -> {
            BatchOut batchOut = new BatchOut();
            batchOut.setBatchId(item.getBatchId());
            batchOut.setCreateDt(LocalDateTime.now());
            batchOut.setStartDt(item.getStartDt());
            batchOut.setEndDt(item.getEndDt());
            batchOut.setRecCnt(item.getRecCnt());
            batchOut.setOutCnt(item.getRecCnt());

            return batchOut;
        };
    }

    @Bean
    MyBatisBatchItemWriter<BatchOut> myBatisItemWriter() {
        return new MyBatisBatchItemWriterBuilder<BatchOut>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId("com.home.quartzapp.batch.repository.BatchRepository.insertBatchOut")
                .itemToParameterConverter(item -> new HashMap<>() {{
                    put("item", item);
                }})
                .build();
    }
}
