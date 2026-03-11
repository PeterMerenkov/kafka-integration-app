package com.example.kafkaintegration.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record PublishRequest(
        String key,
        @NotNull(message = "payload must not be null")
        JsonNode payload
) {
}
