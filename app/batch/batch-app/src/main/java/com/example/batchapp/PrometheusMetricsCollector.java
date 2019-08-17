package com.example.batchapp;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

import java.io.IOException;
import java.util.Collection;

@Slf4j
public class PrometheusMetricsCollector implements MetricsCollector {

    private final PushGateway pushGateway;

    private CollectorRegistry registry;
    private Gauge duration;
    private Gauge lastSuccess;
    private Gauge lastFailure;
    private Gauge processedCount;
    private Gauge.Timer timer;

    PrometheusMetricsCollector(PushGateway pushGateway) {
        this.pushGateway = pushGateway;
        this.registry = new CollectorRegistry();
        this.duration = Gauge.build().name("batch_job_duration_seconds")
                .help("job duration seconds")
                .register(registry);
        this.lastSuccess = Gauge.build().name("batch_job_last_success")
                .help("last success time(unix time)")
                .register(registry);
        this.lastFailure = Gauge.build().name("batch_job_last_failure")
                .help("last failure time(unix time)")
                .register(registry);
        this.processedCount = Gauge.build().name("batch_job_processed_count")
                .help("processed count")
                .labelNames("status")
                .register(registry);
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        timer = duration.startTimer();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        timer.setDuration();

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            lastSuccess.setToCurrentTime();
        } else {
            lastFailure.setToCurrentTime();
        }
        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
        if (!stepExecutions.isEmpty()) {
            // とりあえず1つ目のStepの書込件数(これじゃダメ)
            processedCount.labels(jobExecution.getStatus().toString())
                    .set(stepExecutions.iterator().next().getWriteCount());
        }

        try {
            pushGateway.pushAdd(registry, jobExecution.getJobInstance().getJobName());
        } catch (IOException e) {
            log.warn("failed to send batch metrics...skip", e);
        }
    }
}
