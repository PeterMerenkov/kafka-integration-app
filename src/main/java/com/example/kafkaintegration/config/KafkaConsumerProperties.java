package com.example.kafkaintegration.config;

public interface KafkaConsumerProperties {
    String getTopic();
    String getGroupId();
    int getConcurrency();
    String getAutoOffsetReset();
    boolean isAutoStartup();
    boolean isEnableAutoCommit();
    int getAutoCommitIntervalMs();
    int getMaxPollIntervalMs();
    int getMaxPollRecords();
    boolean isLogEvents();
}
