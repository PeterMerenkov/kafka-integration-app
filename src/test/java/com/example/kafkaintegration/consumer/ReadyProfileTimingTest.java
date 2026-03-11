package com.example.kafkaintegration.consumer;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"ready", "timing"})
class ReadyProfileTimingTest extends AbstractLifecycleTimingTest {

    @Override
    protected String getProfileName() {
        return "ready";
    }
}
