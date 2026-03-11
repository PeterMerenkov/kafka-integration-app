package com.example.kafkaintegration.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;

@Validated
@Setter
public abstract class AbstractKafkaConsumerConfig implements KafkaConsumerProperties {

    private KafkaDefaultProps defaults;

    @Getter
    @NotBlank
    private String topic;
    private String groupId;
    private Integer concurrency;
    private String autoOffsetReset;
    private Boolean autoStartup;
    private Boolean enableAutoCommit;
    private Integer autoCommitIntervalMs;
    private Integer maxPollIntervalMs;
    private Integer maxPollRecords;
    private Boolean logEvents;

    @Autowired
    public void setDefaults(KafkaDefaultProps defaults) {
        this.defaults = defaults;
    }

    @Override
    public String getGroupId() {
        return groupId != null ? groupId : defaults.getConsumer().getGroupId();
    }

    @Override
    public int getConcurrency() {
        return concurrency != null ? concurrency : defaults.getConsumer().getConcurrency();
    }

    @Override
    public String getAutoOffsetReset() {
        return autoOffsetReset != null ? autoOffsetReset : defaults.getConsumer().getAutoOffsetReset();
    }

    @Override
    public boolean isAutoStartup() {
        return autoStartup != null ? autoStartup : defaults.getConsumer().getAutoStartup();
    }

    @Override
    public boolean isEnableAutoCommit() {
        return enableAutoCommit != null ? enableAutoCommit : defaults.getConsumer().getEnableAutoCommit();
    }

    @Override
    public int getAutoCommitIntervalMs() {
        return autoCommitIntervalMs != null ? autoCommitIntervalMs : defaults.getConsumer().getAutoCommitIntervalMs();
    }

    @Override
    public int getMaxPollIntervalMs() {
        return maxPollIntervalMs != null ? maxPollIntervalMs : defaults.getConsumer().getMaxPollIntervalMs();
    }

    @Override
    public int getMaxPollRecords() {
        return maxPollRecords != null ? maxPollRecords : defaults.getConsumer().getMaxPollRecords();
    }

    @Override
    public boolean isLogEvents() {
        return logEvents != null ? logEvents : defaults.getConsumer().getLogEvents();
    }
}
