package com.example.kafkaintegration.consumer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("timing")
public class TimingConsumer implements CustomKafkaConsumer<String, TimingConsumerConfig> {

    private static final AtomicReference<CountDownLatch> LATCH_REF = new AtomicReference<>(new CountDownLatch(1));
    private static final AtomicReference<String> LAST_PAYLOAD = new AtomicReference<>();

    @Override
    public void handle(String message) {
        LAST_PAYLOAD.set(message);
        LATCH_REF.get().countDown();
    }

    public static CountDownLatch getLatch() {
        return LATCH_REF.get();
    }

    public static String getLastPayload() {
        return LAST_PAYLOAD.get();
    }

    public static void resetLatch() {
        LATCH_REF.set(new CountDownLatch(1));
        LAST_PAYLOAD.set(null);
    }
}
