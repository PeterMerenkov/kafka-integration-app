package com.example.kafkaintegration.consumer;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"init", "timing"})
class InitProfileTimingTest extends AbstractLifecycleTimingTest {

    @Override
    protected String getProfileName() {
        return "init";
    }
}
