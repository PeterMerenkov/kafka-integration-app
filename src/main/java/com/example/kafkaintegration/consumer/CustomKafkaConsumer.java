package com.example.kafkaintegration.consumer;

import com.example.kafkaintegration.config.KafkaConsumerProperties;

public interface CustomKafkaConsumer<T, C extends KafkaConsumerProperties> {
    void handle(T message);
}
