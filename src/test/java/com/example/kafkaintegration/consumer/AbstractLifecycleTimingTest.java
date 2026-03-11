package com.example.kafkaintegration.consumer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest(properties = {
        "app.kafka.consumers.timing.topic=timing-topic",
        "app.kafka.consumers.timing.group-id=timing-group",
        "app.kafka.consumers.timing.concurrency=1",
        "app.kafka.consumers.timing.auto-offset-reset=earliest",
        "app.kafka.consumers.timing.auto-startup=true",
        "app.kafka.consumers.timing.enable-auto-commit=true",
        "app.kafka.consumers.timing.auto-commit-interval-ms=1000",
        "app.kafka.consumers.timing.max-poll-interval-ms=300000",
        "app.kafka.consumers.timing.max-poll-records=500",
        "app.kafka.consumers.timing.log-events=false"
})
@EmbeddedKafka(
        partitions = 1,
        topics = { "timing-topic" },
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
abstract class AbstractLifecycleTimingTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaConsumerRegistrationService registrationService;

    @BeforeEach
    void resetLatch() {
        TimingConsumer.resetLatch();
    }

    @Test
    void shouldMeasureRegistrationAndFirstConsume() throws Exception {
        Instant start = registrationService.getLastRegistrationStartAt();
        Instant finish = registrationService.getLastRegistrationFinishedAt();

        assertThat(start).as("registration start time").isNotNull();
        assertThat(finish).as("registration finish time").isNotNull();
        assertThat(finish).isAfterOrEqualTo(start);

        Duration registrationDuration = Duration.between(start, finish);
        String payload = "timing-" + System.nanoTime();
        Instant sendAt = Instant.now();
        kafkaTemplate.send("timing-topic", payload);

        boolean consumed = TimingConsumer.getLatch().await(10, TimeUnit.SECONDS);
        Instant consumedAt = Instant.now();

        assertThat(consumed).as("timing consumer should receive payload").isTrue();
        assertThat(TimingConsumer.getLastPayload()).isEqualTo(payload);

        Duration consumeDuration = Duration.between(sendAt, consumedAt);
        System.out.printf(
                "profile=%s registrationMs=%d firstConsumeMs=%d%n",
                getProfileName(),
                registrationDuration.toMillis(),
                consumeDuration.toMillis()
        );
    }

    protected abstract String getProfileName();
}
