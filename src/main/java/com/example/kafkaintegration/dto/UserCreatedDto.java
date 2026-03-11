package com.example.kafkaintegration.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCreatedDto(
        @NotBlank(message = "userId must not be blank")
        String userId,
        @NotBlank(message = "email must not be blank")
        String email,
        @NotBlank(message = "name must not be blank")
        String name
) {
}
