package com.example.batchapp;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.time.LocalDateTime;
import java.util.Map;

@EnableBatchProcessing
@Configuration
public class BatchConfig {

    @Bean
    Job uploadJob(JobBuilderFactory jobBuilderFactory, MetricsCollector metricsCollector) {

        return jobBuilderFactory.get("uploadJob")
                .start(uploadStep(null))
                .incrementer(new RunIdIncrementer())
                .listener(metricsCollector)
                .build();
    }

    @Bean
    Step uploadStep(StepBuilderFactory stepBuilderFactory) {

        return stepBuilderFactory.get("uploadStep")
                .<EventRecord, EventRecord>chunk(100)
                .reader(eventReader(null, null))
                .processor(eventProcessor(null))
                .writer(eventWriter(null))
                .build();
    }

    @JobScope
    @Bean
    FlatFileItemReader<EventRecord> eventReader(@Value("#{jobParameters['file.name']}") String fileName,
                                                @Value("${file.mount-path}")String mountPath) {

        BeanWrapperFieldSetMapper<EventRecord> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(EventRecord.class);
        mapper.setCustomEditors(Map.of(LocalDateTime.class, new LocalDateTimePropertyEditor()));

        return new FlatFileItemReaderBuilder<EventRecord>()
                .name("eventReader")
                .resource(new FileSystemResource(mountPath + "/" + fileName))
                .delimited()
                .delimiter(",")
                .names(new String[]{"id", "type", "createdAt", "repoName", "repoUrl", "author", "organization"})
                .fieldSetMapper(mapper)
                .build();
    }

    @Bean
    EventProcessor eventProcessor(EventMapper mapper) {
        return new EventProcessor(mapper);
    }

    @Bean
    MyBatisBatchItemWriter<EventRecord> eventWriter(SqlSessionFactory factory) {

        MyBatisBatchItemWriter<EventRecord> writer = new MyBatisBatchItemWriter<>();
        writer.setSqlSessionFactory(factory);
        writer.setStatementId(EventMapper.class.getName() + ".insert");

        return writer;
    }
}
