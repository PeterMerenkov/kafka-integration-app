package com.example.kafkaintegration.service;

import com.example.kafkaintegration.config.KafkaAppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaAppProperties kafkaProperties;

    public void send(String key, String message) {
        String topic = kafkaProperties.getProducerTopic();
        if (topic == null || topic.isBlank()) {
            throw new IllegalStateException("app.kafka.producer-topic must be configured");
        }

        if (key == null || key.isBlank()) {
            kafkaTemplate.send(topic, message);
            return;
        }

        kafkaTemplate.send(topic, key, message);
    }
}
