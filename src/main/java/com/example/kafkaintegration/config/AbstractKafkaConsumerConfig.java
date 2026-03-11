package com.example.kafkaintegration.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;

@Validated
@Setter
public abstract class AbstractKafkaConsumerConfig implements KafkaConsumerProperties {

    private KafkaConsumerDefaultsConfig defaults;

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
    public void setDefaults(KafkaConsumerDefaultsConfig defaults) {
        this.defaults = defaults;
    }

    @Override
    public String getGroupId() {
        return groupId != null ? groupId : defaults.getGroupId();
    }

    @Override
    public int getConcurrency() {
        return concurrency != null ? concurrency : defaults.getConcurrency();
    }

    @Override
    public String getAutoOffsetReset() {
        return autoOffsetReset != null ? autoOffsetReset : defaults.getAutoOffsetReset();
    }

    @Override
    public boolean isAutoStartup() {
        return autoStartup != null ? autoStartup : defaults.getAutoStartup();
    }

    @Override
    public boolean isEnableAutoCommit() {
        return enableAutoCommit != null ? enableAutoCommit : defaults.getEnableAutoCommit();
    }

    @Override
    public int getAutoCommitIntervalMs() {
        return autoCommitIntervalMs != null ? autoCommitIntervalMs : defaults.getAutoCommitIntervalMs();
    }

    @Override
    public int getMaxPollIntervalMs() {
        return maxPollIntervalMs != null ? maxPollIntervalMs : defaults.getMaxPollIntervalMs();
    }

    @Override
    public int getMaxPollRecords() {
        return maxPollRecords != null ? maxPollRecords : defaults.getMaxPollRecords();
    }

    @Override
    public boolean isLogEvents() {
        return logEvents != null ? logEvents : defaults.getLogEvents();
    }
}
