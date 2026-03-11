package com.example.kafkaintegration.consumer;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest(properties = "app.kafka.consumers.order-paid.auto-offset-reset=earliest")
@EmbeddedKafka(
        partitions = 2,
        topics = {
                "demo-topic",
                "user-created-topic",
                "order-paid-topic",
                "inventory-adjusted-topic"
        },
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class CustomKafkaConsumerPropsRegistrarIntegrationTest {

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

    @BeforeEach
    void resetSpies() {
        reset(demoConsumer, userCreatedConsumer, orderPaidConsumer, inventoryAdjustedConsumer);
    }

    @Test
    void shouldRouteMessagesToMatchedConsumers() {
        kafkaTemplate.send("demo-topic", "{\"text\":\"hello-demo\"}");
        kafkaTemplate.send("user-created-topic", "{\"userId\":\"u-1\",\"email\":\"u1@example.com\",\"name\":\"John\"}");
        kafkaTemplate.send("order-paid-topic", "{\"orderId\":\"ord-42\",\"amount\":125.50,\"currency\":\"USD\"}");
        kafkaTemplate.send("inventory-adjusted-topic", "{\"sku\":\"SKU-77\",\"delta\":-3,\"reason\":\"sale\"}");

        verify(demoConsumer, timeout(10000)).handle(argThat(dto -> "hello-demo".equals(dto.text())));
        verify(userCreatedConsumer, timeout(10000)).handle(argThat(dto -> "u-1".equals(dto.userId())));
        verify(orderPaidConsumer, timeout(10000)).handle(argThat(dto ->
                "ord-42".equals(dto.orderId()) && dto.amount().compareTo(new java.math.BigDecimal("125.50")) == 0));
        verify(inventoryAdjustedConsumer, timeout(10000)).handle(argThat(dto -> "SKU-77".equals(dto.sku()) && dto.delta() == -3));
    }

    @Test
    void shouldProcessSequentialMessages() {
        String marker = "second-message-01";

        kafkaTemplate.send("demo-topic", "{\"text\":\"first-message\"}");
        kafkaTemplate.send("demo-topic", "{\"text\":\"" + marker + "\"}");

        verify(demoConsumer, timeout(10000)).handle(argThat(dto -> marker.equals(dto.text())));
    }
}
