package com.example.kafkaintegration.consumer;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"lifecycle", "timing"})
class LifecycleProfileTimingTest extends AbstractLifecycleTimingTest {

    @Override
    protected String getProfileName() {
        return "lifecycle";
    }
}
