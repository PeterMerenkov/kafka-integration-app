package com.example.kafkaintegration.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class UserCreatedConsumerConfig implements KafkaConsumerProperties {

    private final String topic;
    private final String groupId;
    private final int concurrency;
    private final String autoOffsetReset;
    private final boolean autoStartup;
    private final boolean enableAutoCommit;
    private final int autoCommitIntervalMs;
    private final int maxPollIntervalMs;
    private final int maxPollRecords;
    private final boolean logEvents;

    public UserCreatedConsumerConfig(KafkaAppProperties kafkaAppProperties) {
        KafkaAppProperties.ResolvedConsumer resolved = kafkaAppProperties.resolveConsumer("user-created");
        this.topic = resolved.topic();
        this.groupId = resolved.groupId();
        this.concurrency = resolved.concurrency();
        this.autoOffsetReset = resolved.autoOffsetReset();
        this.autoStartup = resolved.autoStartup();
        this.enableAutoCommit = resolved.enableAutoCommit();
        this.autoCommitIntervalMs = resolved.autoCommitIntervalMs();
        this.maxPollIntervalMs = resolved.maxPollIntervalMs();
        this.maxPollRecords = resolved.maxPollRecords();
        this.logEvents = resolved.logEvents();
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public int getConcurrency() {
        return concurrency;
    }

    @Override
    public String getAutoOffsetReset() {
        return autoOffsetReset;
    }

    @Override
    public boolean isAutoStartup() {
        return autoStartup;
    }

    @Override
    public boolean isEnableAutoCommit() {
        return enableAutoCommit;
    }

    @Override
    public int getAutoCommitIntervalMs() {
        return autoCommitIntervalMs;
    }

    @Override
    public int getMaxPollIntervalMs() {
        return maxPollIntervalMs;
    }

    @Override
    public int getMaxPollRecords() {
        return maxPollRecords;
    }

    @Override
    public boolean isLogEvents() {
        return logEvents;
    }
}
