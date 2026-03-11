package com.example.kafkaintegration.dto;

import jakarta.validation.constraints.NotBlank;

public record InventoryAdjustedDto(
        @NotBlank(message = "sku must not be blank")
        String sku,
        int delta,
        @NotBlank(message = "reason must not be blank")
        String reason
) {
}
