package com.home.quartzapp.batch.querydslreader;

import com.home.quartzapp.batch.entity.BatchIn;
import com.home.quartzapp.batch.entity.BatchOut;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
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
    @JobScope
    public Step querydslReaderStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {

        return new StepBuilder("querydslReaderStep", jobRepository)
                .<BatchIn, BatchOut>chunk(3, transactionManager)
                .reader(querydslReaderJobReader())
                .processor(querydslReaderJobProcessor())
                .writer(querydslReaderJobWriter())
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        stepExecution.getJobExecution().getExecutionContext().put("stepStartDate", LocalDateTime.now());
                    }
                })
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<BatchIn> querydslReaderJobReader() {

        QuerydslJpaQueryProvider<BatchIn> queryProvider = new QuerydslJpaQueryProvider<>();

        queryProvider.setJpaQuery(jpaQueryFactory -> jpaQueryFactory
                .selectFrom(batchIn)
                .where(batchIn.batchName.eq("BAT001")
                        .and(batchIn.status.eq("RD"))
                        .and(batchIn.startDt.goe(LocalDateTime.parse("2024-09-02T00:00:00")))
                        .and(batchIn.startDt.lt(LocalDateTime.parse("2024-09-03T00:00:00")))));

        return new JpaCursorItemReaderBuilder<BatchIn>()
                .name("querydslReaderJobReader")
                .entityManagerFactory(entityManagerFactory)
                .queryProvider(queryProvider)
                .maxItemCount(3)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<BatchIn, BatchOut> querydslReaderJobProcessor() {

        return new ItemProcessor<BatchIn, BatchOut>() {
            @Value("#{stepExecution.jobExecution}")
            private JobExecution jobExecution;

            @Override
            public BatchOut process(BatchIn item) throws Exception {
                BatchOut batchOut = new BatchOut();

                batchOut.setBatchId(item.getBatchId());
                batchOut.setCreateDt((LocalDateTime)jobExecution.getExecutionContext().get("stepStartDate"));
                batchOut.setStartDt(item.getStartDt());
                batchOut.setEndDt(item.getEndDt());
                batchOut.setRecCnt(item.getRecCnt());
                batchOut.setOutCnt(item.getRecCnt());

                return batchOut;
            }
        };
    }

    @Bean
    @StepScope
    public JpaItemWriter<BatchOut> querydslReaderJobWriter() {

        return new JpaItemWriterBuilder<BatchOut>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }
}
