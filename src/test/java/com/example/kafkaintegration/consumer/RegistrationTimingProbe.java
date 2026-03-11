package com.example.kafkaintegration.consumer;

import java.time.Instant;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("timing")
public class RegistrationTimingProbe {

    private volatile Instant registrationStartAt;
    private volatile Instant registrationFinishedAt;

    public void markStart(Instant startAt) {
        this.registrationStartAt = startAt;
    }

    public void markFinish(Instant finishAt) {
        this.registrationFinishedAt = finishAt;
    }

    public Instant getRegistrationStartAt() {
        return registrationStartAt;
    }

    public Instant getRegistrationFinishedAt() {
        return registrationFinishedAt;
    }
}
