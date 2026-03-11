package com.example.kafkaintegration.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Configuration
@Validated
@ConfigurationProperties(prefix = "app.kafka.defaults")
public class KafkaConsumerDefaultsConfig {

    @NotBlank
    private String groupId;
    @NotBlank
    private String autoOffsetReset;
    @NotNull
    @Positive
    private Integer concurrency;
    @NotNull
    private Boolean autoStartup;
    @NotNull
    private Boolean enableAutoCommit;
    @NotNull
    @Positive
    private Integer autoCommitIntervalMs;
    @NotNull
    @Positive
    private Integer maxPollIntervalMs;
    @NotNull
    @Positive
    private Integer maxPollRecords;
    @NotNull
    private Boolean logEvents;
}
