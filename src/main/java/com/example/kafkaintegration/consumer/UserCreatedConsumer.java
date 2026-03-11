package com.example.kafkaintegration.consumer;

import com.example.kafkaintegration.config.UserCreatedConsumerConfig;
import com.example.kafkaintegration.dto.UserCreatedDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserCreatedConsumer implements CustomKafkaConsumer<UserCreatedDto, UserCreatedConsumerConfig> {

    @Override
    public void handle(UserCreatedDto dto) {
        log.info("User created event received: {}", dto);
    }
}
