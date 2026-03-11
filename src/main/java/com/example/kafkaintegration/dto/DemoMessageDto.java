package com.example.kafkaintegration.dto;

import jakarta.validation.constraints.NotBlank;

public record DemoMessageDto(
        @NotBlank(message = "text must not be blank")
        String text
) {
}
