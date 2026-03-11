package com.example.kafkaintegration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.kafka.consumers.demo-message")
public class DemoMessageConsumerConfig implements KafkaConsumerProperties {

    private String topic;
    private String groupId;
    private int concurrency;
    private String autoOffsetReset;
    private boolean autoStartup;
    private boolean enableAutoCommit;
    private int autoCommitIntervalMs;
    private int maxPollIntervalMs;
    private int maxPollRecords;
    private boolean logEvents;
}
