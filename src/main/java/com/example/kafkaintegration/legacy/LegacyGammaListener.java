package com.example.kafkaintegration.legacy;

import com.example.kafkaintegration.legacy.dto.LegacyGammaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LegacyGammaListener {

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.legacy.gamma.topic}",
            groupId = "${app.kafka.legacy.gamma.group-id}",
            concurrency = "${app.kafka.legacy.gamma.concurrency}"
    )
    public void onMessage(String payload) {
        try {
            LegacyGammaEvent event = objectMapper.readValue(payload, LegacyGammaEvent.class);
            handle(event);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize legacy gamma event.", e);
        }
    }

    private void handle(LegacyGammaEvent event) {
        // Legacy mock handler.
    }
}
