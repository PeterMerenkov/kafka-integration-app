package com.example.kafkaintegration.legacy;

import com.example.kafkaintegration.legacy.dto.LegacyAlphaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LegacyAlphaListener {

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.legacy.alpha.topic}",
            groupId = "${app.kafka.legacy.alpha.group-id}",
            concurrency = "${app.kafka.legacy.alpha.concurrency}"
    )
    public void onMessage(String payload) {
        try {
            LegacyAlphaEvent event = objectMapper.readValue(payload, LegacyAlphaEvent.class);
            handle(event);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize legacy alpha event.", e);
        }
    }

    private void handle(LegacyAlphaEvent event) {
        // Legacy mock handler.
    }
}
