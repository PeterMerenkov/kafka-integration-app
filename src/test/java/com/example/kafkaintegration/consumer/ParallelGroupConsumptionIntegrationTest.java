package com.example.kafkaintegration.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles({ "parallel-group-test", "lifecycle" })
@SpringBootTest(properties = {
        "app.kafka.consumers.parallel-group.topic=demo-topic",
        "app.kafka.consumers.parallel-group.group-id=parallel-group-1",
        "app.kafka.consumers.parallel-group.concurrency=1",
        "app.kafka.consumers.parallel-group.auto-offset-reset=earliest",
        "app.kafka.consumers.parallel-group.auto-startup=true",
        "app.kafka.consumers.parallel-group.enable-auto-commit=true",
        "app.kafka.consumers.parallel-group.auto-commit-interval-ms=1000",
        "app.kafka.consumers.parallel-group.max-poll-interval-ms=300000",
        "app.kafka.consumers.parallel-group.max-poll-records=500",
        "app.kafka.consumers.parallel-group.log-events=false",
        "app.kafka.consumers.parallel-group-second.topic=demo-topic",
        "app.kafka.consumers.parallel-group-second.group-id=parallel-group-2",
        "app.kafka.consumers.parallel-group-second.concurrency=1",
        "app.kafka.consumers.parallel-group-second.auto-offset-reset=earliest",
        "app.kafka.consumers.parallel-group-second.auto-startup=true",
        "app.kafka.consumers.parallel-group-second.enable-auto-commit=true",
        "app.kafka.consumers.parallel-group-second.auto-commit-interval-ms=1000",
        "app.kafka.consumers.parallel-group-second.max-poll-interval-ms=300000",
        "app.kafka.consumers.parallel-group-second.max-poll-records=500",
        "app.kafka.consumers.parallel-group-second.log-events=false"
})
@EmbeddedKafka(
        partitions = 1,
        topics = { "demo-topic" },
        bootstrapServersProperty = "app.kafka.bootstrap-servers"
)
class ParallelGroupConsumptionIntegrationTest {

    private static final int MESSAGE_COUNT = 6;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        log.info("Resetting test state.");
        ParallelGroupTestConsumer.reset(MESSAGE_COUNT);
        ParallelGroupSecondTestConsumer.reset(MESSAGE_COUNT);
    }

    @Test
    void shouldConsumeMessagesInParallelGroups() throws Exception {
        log.info("Publishing {} messages to demo-topic.", MESSAGE_COUNT);
        List<String> sentPayloads = new ArrayList<>();
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            String payload = "parallel-" + i;
            sentPayloads.add(payload);
            kafkaTemplate.send("demo-topic", "{\"text\":\"" + payload + "\"}");
        }

        log.info("Awaiting consumption by both groups.");
        boolean firstGroupDone = ParallelGroupTestConsumer.awaitMessages(10, TimeUnit.SECONDS);
        boolean secondGroupDone = ParallelGroupSecondTestConsumer.awaitMessages(10, TimeUnit.SECONDS);

        assertThat(firstGroupDone)
                .as("parallel group 1 should consume all %s messages within the timeout", MESSAGE_COUNT)
                .isTrue();
        assertThat(secondGroupDone)
                .as("parallel group 2 should consume all %s messages within the timeout", MESSAGE_COUNT)
                .isTrue();

        Set<String> firstGroupMessages = ParallelGroupTestConsumer.getPayloadsSnapshot();
        Set<String> secondGroupMessages = ParallelGroupSecondTestConsumer.getPayloadsSnapshot();

        assertThat(firstGroupMessages)
                .as("parallel group 1 should receive all published messages")
                .containsExactlyInAnyOrderElementsOf(sentPayloads);
        assertThat(secondGroupMessages)
                .as("parallel group 2 should receive all published messages")
                .containsExactlyInAnyOrderElementsOf(sentPayloads);

        log.info("parallel group 1 count={}, parallel group 2 count={}",
                firstGroupMessages.size(), secondGroupMessages.size());
    }
}
