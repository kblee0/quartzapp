package com.home.quartzapp.batch.jdbcreader;

import com.home.quartzapp.batch.entity.BatchJobExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class JdbcReaderJobConfig {
    @Bean
    public Job jdbcReaderJob(JobRepository jobRepository, DataSource dataSource, PlatformTransactionManager transactionManager) {
        Job job = new JobBuilder("jdbcReaderJob", jobRepository)
                .start(jdbcReaderStep(jobRepository, dataSource, transactionManager))
                .build();
        return job;
    }

    @Bean
    public Step jdbcReaderStep(JobRepository jobRepository, DataSource dataSource, PlatformTransactionManager transactionManager) {
        Step step = new StepBuilder("jdbcReaderStep", jobRepository)
                .<BatchJobExecution,BatchJobExecution>chunk(5, transactionManager)
                .reader(jdbcReaderItemReader(dataSource))
                .writer(jdbcReaderItemWriter())
                .build();
        return step;
    }

    @Bean
    public JdbcCursorItemReader<BatchJobExecution> jdbcReaderItemReader(DataSource dataSource) {
        String SQL_READER = """
            select B.JOB_NAME, A.JOB_EXECUTION_ID, A.START_TIME, A.END_TIME, A.STATUS, A.EXIT_CODE, A.EXIT_MESSAGE
            from BATCH_JOB_EXECUTION A
            join BATCH_JOB_INSTANCE B ON A.JOB_EXECUTION_ID = B.JOB_INSTANCE_ID
            where B.JOB_NAME = :jobName
            """;
        Map<String, Object> params = new HashMap<String, Object>() {{
            put("jobName", "simpleJob");
        }};
        JdbcCursorItemReader<BatchJobExecution> reader = new JdbcCursorItemReaderBuilder<BatchJobExecution>()
                .name("jdbcReaderItemReader")
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<BatchJobExecution>(BatchJobExecution.class))
                .sql(NamedParameterUtils.substituteNamedParameters(SQL_READER, new MapSqlParameterSource(params)))
                .preparedStatementSetter(new ArgumentPreparedStatementSetter(NamedParameterUtils.buildValueArray(SQL_READER, params)))
                .build();

        return reader;
    }

    @Bean
    public ItemWriter<BatchJobExecution> jdbcReaderItemWriter() {
        return chunk -> {
            log.info("Writer: {}", chunk);
        };
    }
}
