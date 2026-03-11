package com.example.kafkaintegration.consumer.registrar;

import com.example.kafkaintegration.consumer.KafkaConsumerRegistrationService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("init")
@ConditionalOnProperty(name = "app.kafka.consumers-enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class KafkaConsumerInitRegistrar implements SmartInitializingSingleton {

    private final KafkaConsumerRegistrationService registrationService;

    @Override
    public void afterSingletonsInstantiated() {
        registrationService.startAll();
    }

    @PreDestroy
    public void shutdown() {
        registrationService.stopAll();
    }
}
