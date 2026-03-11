package com.example.kafkaintegration.consumer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest(properties = "app.kafka.consumers.demo-message.log-events=true")
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "demo-topic"
        },
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@ExtendWith(OutputCaptureExtension.class)
class CustomKafkaConsumerLogEventsIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void shouldLogEventsWhenEnabled(CapturedOutput output) {
        String key = "log-key-1";
        String marker = "log-event-01";

        kafkaTemplate.send("demo-topic", key, "{\"text\":\"" + marker + "\"}");

        String expectedTopic = "Kafka event topic='demo-topic'";
        String expectedKey = "key='" + key + "'";
        waitForLog(output, expectedTopic, expectedKey, marker);
    }

    private void waitForLog(CapturedOutput output, String expectedTopic, String expectedKey, String marker) {
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            String out = output.getOut();
            if (out.contains(expectedTopic) && out.contains(expectedKey) && out.contains(marker)) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        assertThat(output.getOut())
                .contains(expectedTopic)
                .contains(expectedKey)
                .contains(marker);
    }
}
