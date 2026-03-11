package com.example.kafkaintegration.consumer;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"runner", "timing"})
class RunnerProfileTimingTest extends AbstractLifecycleTimingTest {

    @Override
    protected String getProfileName() {
        return "runner";
    }
}
