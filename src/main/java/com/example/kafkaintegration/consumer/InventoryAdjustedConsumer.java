package com.example.kafkaintegration.consumer;

import com.example.kafkaintegration.config.InventoryAdjustedConsumerConfig;
import com.example.kafkaintegration.dto.InventoryAdjustedDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryAdjustedConsumer implements CustomKafkaConsumer<InventoryAdjustedDto, InventoryAdjustedConsumerConfig> {

    @Override
    public void handle(InventoryAdjustedDto dto) {
        log.info("Inventory adjusted event received: {}", dto);
    }
}
