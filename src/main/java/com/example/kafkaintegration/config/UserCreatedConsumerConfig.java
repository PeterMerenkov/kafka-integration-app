package com.example.kafkaintegration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties(prefix = "app.kafka.consumers.user-created")
public class UserCreatedConsumerConfig extends AbstractKafkaConsumerConfig {
}
