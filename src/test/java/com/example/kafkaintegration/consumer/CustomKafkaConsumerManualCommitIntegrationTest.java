package com.example.kafkaintegration.consumer;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest(properties = "app.kafka.consumers.demo-message.enable-auto-commit=false")
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "demo-topic"
        },
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class CustomKafkaConsumerManualCommitIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @SpyBean
    private KafkaMessageListener demoConsumer;

    @BeforeEach
    void resetSpies() {
        reset(demoConsumer);
    }

    @Test
    void shouldProcessMessagesWithManualAck() {
        String marker = "manual-ack-ok";

        kafkaTemplate.send("demo-topic", "{\"text\":\"" + marker + "\"}");

        verify(demoConsumer, timeout(10000)).handle(argThat(dto -> marker.equals(dto.text())));
    }
}
