package com.home.quartzapp.batch.mybatis2;

import com.home.quartzapp.batch.entity.BatchIn;
import com.home.quartzapp.batch.service.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.builder.MyBatisCursorItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MyBatis2JobConfig {
    private final SqlSessionFactory sqlSessionFactory;
    private final BatchService batchService;
    private final JobRepository jobRepository;

    private class SkipErrorException extends Exception {
        SkipErrorException(Throwable cause) {
            super(cause);
        }
    }

    private class SkpipOnErrorPolicy implements SkipPolicy {
        @Override
        public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
            return t instanceof SkipErrorException;
        }
    }

    @Bean
    public Job myBatis2Job(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new JobBuilder("myBatis2Job", jobRepository)
                .start(myBatis2Step(jobRepository, platformTransactionManager))
                .build();
    }

    @Bean
    public Step myBatis2Step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("myBatis2Step", jobRepository)
                .<BatchIn,BatchIn>chunk(3, platformTransactionManager)
                .reader(myBatis2ItemReader())
                .writer(myBatis2ItemWriter())
                .faultTolerant()
                .retryLimit(0)
                .skipPolicy(new SkpipOnErrorPolicy())
                .build();
    }

    @Bean
    public MyBatisCursorItemReader<BatchIn> myBatis2ItemReader() {
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
    ItemWriter<BatchIn> myBatis2ItemWriter() {
        return chunk -> {
            for (BatchIn item : chunk.getItems()) {
                try {
                    batchService.createBatchOut(item);
                } catch (RuntimeException e) {
                    batchService.forceUpdateBatchInStatus(item.getBatchId(), "ER");
                    throw new SkipErrorException(e);
                }
            }
        };
    }
}
