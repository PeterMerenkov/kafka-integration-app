package com.example.kafkaintegration.consumer;

import com.example.kafkaintegration.config.AbstractKafkaConsumerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

@Profile("parallel-group-test")
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.kafka.consumers.parallel-group-second")
public class ParallelGroupSecondTestConsumerConfig extends AbstractKafkaConsumerConfig {
}
