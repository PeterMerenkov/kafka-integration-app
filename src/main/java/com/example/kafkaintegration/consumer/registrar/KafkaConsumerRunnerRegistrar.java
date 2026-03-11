package com.example.kafkaintegration.consumer.registrar;

import com.example.kafkaintegration.consumer.KafkaConsumerRegistrationService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("runner")
@ConditionalOnProperty(name = "app.kafka.consumers-enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class KafkaConsumerRunnerRegistrar implements ApplicationRunner {

    private final KafkaConsumerRegistrationService registrationService;

    @Override
    public void run(ApplicationArguments args) {
        registrationService.startAll();
    }

    @PreDestroy
    public void shutdown() {
        registrationService.stopAll();
    }
}
