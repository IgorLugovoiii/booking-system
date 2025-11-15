package com.example.logging.config;

import com.example.logging.aspect.LoggingAspect;
import com.example.logging.filter.LoggingFilter;
import com.example.logging.handler.GlobalExceptionHandler;
import com.example.logging.producer.LogProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

@AutoConfiguration
@ConditionalOnProperty(
        prefix = "logging.kafka",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class LoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LogProducer logProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        return new LogProducer(kafkaTemplate, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggingAspect loggingAspect(LogProducer logProducer) {
        return new LoggingAspect(logProducer);
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(LogProducer logProducer) {
        return new GlobalExceptionHandler(logProducer);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication
    public OncePerRequestFilter loggingFilter() {
        return new LoggingFilter();
    }
}
