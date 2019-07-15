package com.example.batchapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class EventProcessor implements ItemProcessor<EventRecord, EventRecord> {

    private final EventMapper mapper;

    public EventProcessor(EventMapper mapper) {
        this.mapper = mapper;
    }

    @BeforeStep
    private void beforeStep(StepExecution stepExecution) {
        log.info("start event processor");
    }

    @AfterStep
    private void afterStep(StepExecution stepExecution) {
        log.info("completed event processor");
    }

    @Override
    public EventRecord process(EventRecord item) {

        log.info("[{}:{}] Processing...", item.getAuthor(), item.getRepoName());

        if (mapper.countById(item.getId()) != 0) {
            log.warn("[{}:{}] already exists. skip this record!", item.getAuthor(), item.getRepoName());
            return null;
        }
        return item;
    }
}
