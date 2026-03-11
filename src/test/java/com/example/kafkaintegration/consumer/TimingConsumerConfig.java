package com.example.kafkaintegration.consumer;

import com.example.kafkaintegration.config.KafkaConsumerProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Data
@Configuration
@Profile("timing")
@ConfigurationProperties(prefix = "app.kafka.consumers.timing")
public class TimingConsumerConfig implements KafkaConsumerProperties {

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
