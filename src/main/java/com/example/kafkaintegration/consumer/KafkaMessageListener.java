package com.example.kafkaintegration.consumer;

import com.example.kafkaintegration.config.DemoMessageConsumerConfig;
import com.example.kafkaintegration.dto.DemoMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaMessageListener implements CustomKafkaConsumer<DemoMessageDto, DemoMessageConsumerConfig> {

    @Override
    public void handle(DemoMessageDto dto) {
        log.info("Received DTO from Kafka: {}", dto);
    }
}
