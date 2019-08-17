package com.example.batchapp;

import io.prometheus.client.exporter.PushGateway;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @ConditionalOnProperty(name = "prometheus.enabled", havingValue = "true")
    @Configuration
    class PrometheusConfig {

        @Bean
        PushGateway pushGateway(@Value("${prometheus.push-address}") String address) {
            return new PushGateway(address);
        }

        @JobScope
        @Bean
        MetricsCollector metricsCollector() {
            return new PrometheusMetricsCollector(pushGateway(null));
        }
    }

    @ConditionalOnProperty(name = "prometheus.enabled", havingValue = "false", matchIfMissing = true)
    @Bean
    MetricsCollector noopCollector() {
        return new MetricsCollector() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                // no-op
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                // no-op
            }
        };
    }
}
