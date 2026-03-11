package com.example.kafkaintegration.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Configuration
@Validated
@ConfigurationProperties(prefix = "app.kafka.defaults")
public class KafkaDefaultProps {

    @NotEmpty
    private List<String> bootstrapServers;

    @NestedConfigurationProperty
    private SslProps ssl;

    @NotNull
    @Valid
    @NestedConfigurationProperty
    private ConsumerProps consumer;

    @Getter
    @Setter
    public static class SslProps {
        private String securityProtocol;
        private String keystoreType;
        private String keystoreLocation;
        private String keystorePassword;
        private String truststoreType;
        private String truststoreLocation;
        private String truststorePassword;
    }

    @Getter
    @Setter
    public static class ConsumerProps {
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
}
