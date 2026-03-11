package com.example.kafkaintegration.consumer;

import com.example.kafkaintegration.dto.DemoMessageDto;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("parallel-group-test")
@Component
public class ParallelGroupTestConsumer implements CustomKafkaConsumer<DemoMessageDto, ParallelGroupTestConsumerConfig> {

    private static final Object LOCK = new Object();
    private static CountDownLatch latch = new CountDownLatch(0);
    private static final Set<String> payloads = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void handle(DemoMessageDto message) {
        payloads.add(message.text());
        latch.countDown();
    }

    static void reset(int expectedMessages) {
        synchronized (LOCK) {
            payloads.clear();
            latch = new CountDownLatch(expectedMessages);
        }
    }

    static boolean awaitMessages(long timeout, TimeUnit unit) throws InterruptedException {
        return latch.await(timeout, unit);
    }

    static Set<String> getPayloadsSnapshot() {
        synchronized (LOCK) {
            return new HashSet<>(payloads);
        }
    }
}
