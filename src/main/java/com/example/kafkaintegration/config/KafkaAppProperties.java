package com.example.kafkaintegration.config;

import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaAppProperties {

    @NotBlank
    private String producerTopic;

    @Valid
    @NotNull
    private Defaults defaults;

    @Valid
    @NotEmpty
    private Map<String, Consumer> consumers = new LinkedHashMap<>();

    public ResolvedConsumer resolveConsumer(String consumerKey) {
        Consumer consumerConfig = consumers.get(consumerKey);
        if (consumerConfig == null) {
            throw new BeanCreationException("No consumer config found for key '" + consumerKey + "'");
        }
        if (!StringUtils.hasText(consumerConfig.getTopic())) {
            throw new BeanCreationException("Consumer '" + consumerKey + "' must define topic");
        }

        return new ResolvedConsumer(
                consumerConfig.getTopic(),
                firstNonBlank(consumerConfig.getGroupId(), defaults.getGroupId()),
                firstNonBlank(consumerConfig.getAutoOffsetReset(), defaults.getAutoOffsetReset()),
                firstNonNull(consumerConfig.getConcurrency(), defaults.getConcurrency()),
                firstNonNull(consumerConfig.getAutoStartup(), defaults.getAutoStartup()),
                firstNonNull(consumerConfig.getEnableAutoCommit(), defaults.getEnableAutoCommit()),
                firstNonNull(consumerConfig.getAutoCommitIntervalMs(), defaults.getAutoCommitIntervalMs()),
                firstNonNull(consumerConfig.getMaxPollIntervalMs(), defaults.getMaxPollIntervalMs()),
                firstNonNull(consumerConfig.getMaxPollRecords(), defaults.getMaxPollRecords()),
                firstNonNull(consumerConfig.getLogEvents(), defaults.getLogEvents())
        );
    }

    private String firstNonBlank(String first, String second) {
        return StringUtils.hasText(first) ? first : second;
    }

    private int firstNonNull(Integer first, int second) {
        return first != null ? first : second;
    }

    private boolean firstNonNull(Boolean first, boolean second) {
        return first != null ? first : second;
    }

    @Getter
    @Setter
    public static class Defaults {

        @NotBlank
        private String groupId;

        @NotBlank
        private String autoOffsetReset;

        @Min(1)
        private int concurrency;

        @NotNull
        private Boolean autoStartup;

        @NotNull
        private Boolean enableAutoCommit;

        @Min(1)
        private int autoCommitIntervalMs;

        @Min(1)
        private int maxPollIntervalMs;

        @Min(1)
        private int maxPollRecords;

        @NotNull
        private Boolean logEvents;
    }

    @Getter
    @Setter
    public static class Consumer {

        @NotBlank
        private String topic;
        private String groupId;
        private String autoOffsetReset;
        @Min(1)
        private Integer concurrency;
        private Boolean autoStartup;
        private Boolean enableAutoCommit;
        @Min(1)
        private Integer autoCommitIntervalMs;
        @Min(1)
        private Integer maxPollIntervalMs;
        @Min(1)
        private Integer maxPollRecords;
        private Boolean logEvents;
    }

    public record ResolvedConsumer(
            String topic,
            String groupId,
            String autoOffsetReset,
            int concurrency,
            boolean autoStartup,
            boolean enableAutoCommit,
            int autoCommitIntervalMs,
            int maxPollIntervalMs,
            int maxPollRecords,
            boolean logEvents
    ) {
    }
}
