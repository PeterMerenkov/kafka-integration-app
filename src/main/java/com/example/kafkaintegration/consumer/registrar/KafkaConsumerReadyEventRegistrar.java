package com.example.kafkaintegration.consumer.registrar;

import com.example.kafkaintegration.consumer.KafkaConsumerRegistrationService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("ready")
@ConditionalOnProperty(name = "app.kafka.consumers-enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class KafkaConsumerReadyEventRegistrar {

    private final KafkaConsumerRegistrationService registrationService;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        registrationService.startAll();
    }

    @PreDestroy
    public void shutdown() {
        registrationService.stopAll();
    }
}
