package com.example.kafkaintegration.legacy;

import com.example.kafkaintegration.legacy.dto.LegacyBetaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LegacyBetaListener {

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.legacy.beta.topic}",
            groupId = "${app.kafka.legacy.beta.group-id}",
            concurrency = "${app.kafka.legacy.beta.concurrency}"
    )
    public void onMessage(String payload) {
        try {
            LegacyBetaEvent event = objectMapper.readValue(payload, LegacyBetaEvent.class);
            handle(event);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize legacy beta event.", e);
        }
    }

    private void handle(LegacyBetaEvent event) {
        // Legacy mock handler.
    }
}
