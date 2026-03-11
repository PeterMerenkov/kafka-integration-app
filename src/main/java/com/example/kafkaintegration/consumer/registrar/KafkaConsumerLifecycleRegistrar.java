package com.example.kafkaintegration.consumer.registrar;

import com.example.kafkaintegration.consumer.KafkaConsumerRegistrationService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"lifecycle", "default"})
@ConditionalOnProperty(name = "app.kafka.consumers-enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class KafkaConsumerLifecycleRegistrar implements SmartLifecycle {

    private final KafkaConsumerRegistrationService registrationService;
    private volatile boolean running;

    @Override
    public void start() {
        if (running) {
            return;
        }
        registrationService.startAll();
        running = true;
    }

    @Override
    public void stop() {
        registrationService.stopAll();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @PreDestroy
    public void shutdown() {
        registrationService.stopAll();
        running = false;
    }
}
