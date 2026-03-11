package com.example.kafkaintegration.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class KafkaAppPropertiesResolveConsumerTest {

    @Test
    void shouldApplyDefaultsAndOverrides() {
        KafkaAppProperties properties = new KafkaAppProperties();
        properties.setProducerTopic("demo-topic");

        KafkaAppProperties.Defaults defaults = new KafkaAppProperties.Defaults();
        defaults.setGroupId("default-group");
        defaults.setAutoOffsetReset("earliest");
        defaults.setConcurrency(1);
        defaults.setAutoStartup(true);
        defaults.setEnableAutoCommit(true);
        defaults.setAutoCommitIntervalMs(1000);
        defaults.setMaxPollIntervalMs(300000);
        defaults.setMaxPollRecords(500);
        defaults.setLogEvents(false);
        properties.setDefaults(defaults);

        KafkaAppProperties.Consumer consumer = new KafkaAppProperties.Consumer();
        consumer.setTopic("orders-topic");
        consumer.setGroupId("orders-group");
        consumer.setAutoOffsetReset("latest");
        consumer.setConcurrency(3);
        consumer.setAutoStartup(false);
        consumer.setEnableAutoCommit(false);
        consumer.setAutoCommitIntervalMs(2000);
        consumer.setMaxPollIntervalMs(400000);
        consumer.setMaxPollRecords(50);
        consumer.setLogEvents(true);

        Map<String, KafkaAppProperties.Consumer> consumers = new LinkedHashMap<>();
        consumers.put("orders", consumer);
        properties.setConsumers(consumers);

        KafkaAppProperties.ResolvedConsumer resolved = properties.resolveConsumer("orders");

        assertThat(resolved.topic()).isEqualTo("orders-topic");
        assertThat(resolved.groupId()).isEqualTo("orders-group");
        assertThat(resolved.autoOffsetReset()).isEqualTo("latest");
        assertThat(resolved.concurrency()).isEqualTo(3);
        assertThat(resolved.autoStartup()).isFalse();
        assertThat(resolved.enableAutoCommit()).isFalse();
        assertThat(resolved.autoCommitIntervalMs()).isEqualTo(2000);
        assertThat(resolved.maxPollIntervalMs()).isEqualTo(400000);
        assertThat(resolved.maxPollRecords()).isEqualTo(50);
        assertThat(resolved.logEvents()).isTrue();
    }
}
