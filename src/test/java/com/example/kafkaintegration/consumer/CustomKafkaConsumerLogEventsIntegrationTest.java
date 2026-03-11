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
    void shouldNotLogEventsWithoutAspect(CapturedOutput output) {
        String key = "log-key-1";
        kafkaTemplate.send("demo-topic", key, "{\"text\":\"log-event-01\"}");

        String expectedTopic = "Kafka event topic='demo-topic'";
        waitForNoLog(output, expectedTopic);
    }

    private void waitForNoLog(CapturedOutput output, String expectedTopic) {
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            String out = output.getOut();
            assertThat(out).doesNotContain(expectedTopic);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
