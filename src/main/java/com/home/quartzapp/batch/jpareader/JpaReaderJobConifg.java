package com.home.quartzapp.batch.jpareader;

import com.home.quartzapp.batch.entity.BatchIn;
import com.home.quartzapp.batch.entity.BatchOut;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JpaReaderJobConifg {
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job jpaReaderJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {

        return new JobBuilder("JpaReaderJob", jobRepository)
                .start(jpaReaderStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    @JobScope
    public Step jpaReaderStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {

        return new StepBuilder("JpaReaderStep", jobRepository)
                .<BatchIn, BatchOut>chunk(3, transactionManager)
                .reader(jpaCursorItemReader())
                .processor(jpaItemProcessor())
                .writer(jpaItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<BatchIn> jpaCursorItemReader() {
        final String JPQL = """
                select p
                from BatchIn p
                where batchName = 'BAT001'
                AND startDt >= :from
                AND startDt <  :to
                AND status = :status
                """;
        Map<String, Object> parameters = new HashMap<>() {{
            put("from", LocalDateTime.parse("2024-09-02T00:00:00"));
            put("to", LocalDateTime.parse("2024-09-03T00:00:00"));
            put("status", "RD");
        }};

        return new JpaCursorItemReaderBuilder<BatchIn>()
                .name("jpaCursorItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(JPQL)
                .parameterValues(parameters)
                .maxItemCount(3)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<BatchIn, BatchOut> jpaItemProcessor() {

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
    @StepScope
    public JpaItemWriter<BatchOut> jpaItemWriter() {

        return new JpaItemWriterBuilder<BatchOut>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }
}
