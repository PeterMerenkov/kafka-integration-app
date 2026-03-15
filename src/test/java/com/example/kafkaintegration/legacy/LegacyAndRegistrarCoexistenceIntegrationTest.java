package com.example.kafkaintegration.legacy;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.example.kafkaintegration.consumer.InventoryAdjustedConsumer;
import com.example.kafkaintegration.consumer.KafkaMessageListener;
import com.example.kafkaintegration.consumer.OrderPaidConsumer;
import com.example.kafkaintegration.consumer.UserCreatedConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest(properties = {
        "app.kafka.consumers.order-paid.auto-offset-reset=earliest",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "demo-topic",
                "user-created-topic",
                "order-paid-topic",
                "inventory-adjusted-topic",
                "legacy-a-topic",
                "legacy-b-topic",
                "legacy-c-topic"
        },
        bootstrapServersProperty = "app.kafka.bootstrap-servers"
)
class LegacyAndRegistrarCoexistenceIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @SpyBean
    private KafkaMessageListener demoConsumer;

    @SpyBean
    private UserCreatedConsumer userCreatedConsumer;

    @SpyBean
    private OrderPaidConsumer orderPaidConsumer;

    @SpyBean
    private InventoryAdjustedConsumer inventoryAdjustedConsumer;

    @SpyBean
    private LegacyAlphaListener legacyAlphaListener;

    @SpyBean
    private LegacyBetaListener legacyBetaListener;

    @SpyBean
    private LegacyGammaListener legacyGammaListener;

    @BeforeEach
    void resetSpies() {
        reset(
                demoConsumer,
                userCreatedConsumer,
                orderPaidConsumer,
                inventoryAdjustedConsumer,
                legacyAlphaListener,
                legacyBetaListener,
                legacyGammaListener
        );
    }

    @Test
    void shouldProcessRegistrarAndLegacyConsumersSideBySide() {
        kafkaTemplate.send("demo-topic", "{\"text\":\"demo-1\"}");
        kafkaTemplate.send("user-created-topic", "{\"userId\":\"u-1\",\"email\":\"u1@example.com\",\"name\":\"John\"}");
        kafkaTemplate.send("order-paid-topic", "{\"orderId\":\"ord-42\",\"amount\":125.50,\"currency\":\"USD\"}");
        kafkaTemplate.send("inventory-adjusted-topic", "{\"sku\":\"SKU-77\",\"delta\":-3,\"reason\":\"sale\"}");

        kafkaTemplate.send("legacy-a-topic", "{\"id\":\"a-1\",\"name\":\"Alpha\"}");
        kafkaTemplate.send("legacy-b-topic", "{\"code\":\"b-1\",\"count\":7}");
        kafkaTemplate.send("legacy-c-topic", "{\"reference\":\"c-1\",\"active\":true}");

        verify(demoConsumer, timeout(10000)).handle(argThat(dto -> "demo-1".equals(dto.text())));
        verify(userCreatedConsumer, timeout(10000)).handle(argThat(dto -> "u-1".equals(dto.userId())));
        verify(orderPaidConsumer, timeout(10000)).handle(argThat(dto ->
                "ord-42".equals(dto.orderId()) && dto.amount().compareTo(new java.math.BigDecimal("125.50")) == 0));
        verify(inventoryAdjustedConsumer, timeout(10000)).handle(argThat(dto -> "SKU-77".equals(dto.sku()) && dto.delta() == -3));

        verify(legacyAlphaListener, timeout(10000)).onMessage(argThat(payload -> payload.contains("\"id\":\"a-1\"")));
        verify(legacyBetaListener, timeout(10000)).onMessage(argThat(payload -> payload.contains("\"code\":\"b-1\"")));
        verify(legacyGammaListener, timeout(10000)).onMessage(argThat(payload -> payload.contains("\"reference\":\"c-1\"")));
    }
}
