package com.example.kafkaintegration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record OrderPaidDto(
        @NotBlank(message = "orderId must not be blank")
        String orderId,
        @Positive(message = "amount must be positive")
        BigDecimal amount,
        @NotBlank(message = "currency must not be blank")
        String currency
) {
}
