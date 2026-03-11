package com.example.kafkaintegration.consumer;

import com.example.kafkaintegration.config.OrderPaidConsumerConfig;
import com.example.kafkaintegration.dto.OrderPaidDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderPaidConsumer implements CustomKafkaConsumer<OrderPaidDto, OrderPaidConsumerConfig> {

    @Override
    public void handle(OrderPaidDto dto) {
        log.info("Order paid event received: {}", dto);
    }
}
