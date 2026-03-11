package com.example.kafkaintegration.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class KafkaAppPropertiesValidationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ConfigurationPropertiesAutoConfiguration.class,
                    ValidationAutoConfiguration.class
            ))
            .withUserConfiguration(AppConfig.class);

    @Test
    void shouldFailWhenDefaultsGroupIdIsMissing() {
        contextRunner
                .withPropertyValues(
                        "app.kafka.producer-topic=demo-topic",
                        "app.kafka.defaults.autoOffsetReset=earliest",
                        "app.kafka.defaults.concurrency=1",
                        "app.kafka.defaults.autoStartup=true",
                        "app.kafka.defaults.enableAutoCommit=true",
                        "app.kafka.defaults.autoCommitIntervalMs=1000",
                        "app.kafka.defaults.maxPollIntervalMs=300000",
                        "app.kafka.defaults.maxPollRecords=500",
                        "app.kafka.defaults.logEvents=false",
                        "app.kafka.consumers.demo.topic=demo-topic"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(BindValidationException.class)
                            .hasStackTraceContaining("app.kafka.defaults.groupId");
                });
    }

    @Test
    void shouldFailWhenConsumerTopicIsMissing() {
        contextRunner
                .withPropertyValues(
                        "app.kafka.producer-topic=demo-topic",
                        "app.kafka.defaults.groupId=kafka-integration-group",
                        "app.kafka.defaults.autoOffsetReset=earliest",
                        "app.kafka.defaults.concurrency=1",
                        "app.kafka.defaults.autoStartup=true",
                        "app.kafka.defaults.enableAutoCommit=true",
                        "app.kafka.defaults.autoCommitIntervalMs=1000",
                        "app.kafka.defaults.maxPollIntervalMs=300000",
                        "app.kafka.defaults.maxPollRecords=500",
                        "app.kafka.defaults.logEvents=false",
                        "app.kafka.consumers.demo.groupId=custom-group"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(BindValidationException.class)
                            .hasStackTraceContaining("app.kafka.consumers.demo.topic");
                });
    }

    @Test
    void shouldFailWhenDefaultsMaxPollRecordsIsInvalid() {
        contextRunner
                .withPropertyValues(
                        "app.kafka.producer-topic=demo-topic",
                        "app.kafka.defaults.groupId=kafka-integration-group",
                        "app.kafka.defaults.autoOffsetReset=earliest",
                        "app.kafka.defaults.concurrency=1",
                        "app.kafka.defaults.autoStartup=true",
                        "app.kafka.defaults.enableAutoCommit=true",
                        "app.kafka.defaults.autoCommitIntervalMs=1000",
                        "app.kafka.defaults.maxPollIntervalMs=300000",
                        "app.kafka.defaults.maxPollRecords=0",
                        "app.kafka.defaults.logEvents=false",
                        "app.kafka.consumers.demo.topic=demo-topic"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(BindValidationException.class)
                            .hasStackTraceContaining("app.kafka.defaults.maxPollRecords");
                });
    }

    @Test
    void shouldFailWhenConsumerMaxPollIntervalIsInvalid() {
        contextRunner
                .withPropertyValues(
                        "app.kafka.producer-topic=demo-topic",
                        "app.kafka.defaults.groupId=kafka-integration-group",
                        "app.kafka.defaults.autoOffsetReset=earliest",
                        "app.kafka.defaults.concurrency=1",
                        "app.kafka.defaults.autoStartup=true",
                        "app.kafka.defaults.enableAutoCommit=true",
                        "app.kafka.defaults.autoCommitIntervalMs=1000",
                        "app.kafka.defaults.maxPollIntervalMs=300000",
                        "app.kafka.defaults.maxPollRecords=500",
                        "app.kafka.defaults.logEvents=false",
                        "app.kafka.consumers.demo.topic=demo-topic",
                        "app.kafka.consumers.demo.maxPollIntervalMs=0"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(BindValidationException.class)
                            .hasStackTraceContaining("app.kafka.consumers.demo.maxPollIntervalMs");
                });
    }
}
