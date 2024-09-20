package com.home.quartzapp.batch.querydslreader;

import com.home.quartzapp.batch.entity.BatchIn;
import com.home.quartzapp.batch.entity.BatchOut;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

import static com.home.quartzapp.batch.entity.QBatchIn.batchIn;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class QuerydslReaderJobConfig {
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job querydslReaderJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {

        return new JobBuilder("QuerydslReaderJob", jobRepository)
                .start(querydslReaderStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step querydslReaderStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {

        return new StepBuilder("querydslReaderStep", jobRepository)
                .<BatchIn, BatchOut>chunk(3, transactionManager)
                .reader(querydslReaderJobReader())
                .processor(querydslReaderJobProcessor())
                .writer(querydslReaderJobWriter())
                .build();
    }

    @Bean
    public JpaCursorItemReader<BatchIn> querydslReaderJobReader() {

        return new JpaCursorItemReaderBuilder<BatchIn>()
                .name("querydslReaderJobReader")
                .entityManagerFactory(entityManagerFactory)
                .queryProvider(new QQueryProvider<>(jpaQueryFactory -> jpaQueryFactory
                        .selectFrom(batchIn)
                        .where(batchIn.batchName.eq("BAT001")
                                .and(batchIn.status.eq("RD"))
                                .and(batchIn.startDt.goe(LocalDateTime.parse("2024-09-02T00:00:00")))
                                .and(batchIn.startDt.lt(LocalDateTime.parse("2024-09-03T00:00:00")))
                        )
                ))
                .maxItemCount(3)
                .build();
    }

    @Bean
    public ItemProcessor<BatchIn, BatchOut> querydslReaderJobProcessor() {

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
    public JpaItemWriter<BatchOut> querydslReaderJobWriter() {

        return new JpaItemWriterBuilder<BatchOut>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }
}
