package com.example.kafkaintegration.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kafkaintegration.KafkaIntegrationApplication;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.validation.FieldError;

class ConfigValidationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(KafkaIntegrationApplication.class);

    @Test
    void shouldFailWhenDefaultsGroupIdMissing() {
        List<String> props = baseProperties();
        props.remove("app.kafka.defaults.consumer.group-id=kafka-integration-group");

        contextRunner.withPropertyValues(props.toArray(String[]::new))
                .run(context -> {
                    assertThat(context).hasFailed();
                    BindValidationException failure = getBindValidationFailure(context.getStartupFailure());
                    assertHasFieldError(failure, "groupId");
                });
    }

    @Test
    void shouldFailWhenBootstrapServersMissing() {
        List<String> props = baseProperties();
        props.remove("app.kafka.defaults.bootstrap-servers=localhost:9092");
        props.add("app.kafka.defaults.bootstrap-servers=");

        contextRunner.withPropertyValues(props.toArray(String[]::new))
                .run(context -> {
                    assertThat(context).hasFailed();
                    BindValidationException failure = getBindValidationFailure(context.getStartupFailure());
                    assertHasFieldError(failure, "bootstrapServers");
                });
    }

    @Test
    void shouldFailWhenDefaultsAutoOffsetResetMissing() {
        List<String> props = baseProperties();
        props.remove("app.kafka.defaults.consumer.auto-offset-reset=earliest");

        contextRunner.withPropertyValues(props.toArray(String[]::new))
                .run(context -> {
                    assertThat(context).hasFailed();
                    BindValidationException failure = getBindValidationFailure(context.getStartupFailure());
                    assertHasFieldError(failure, "autoOffsetReset");
                });
    }

    @Test
    void shouldFailWhenDefaultsConcurrencyNonPositive() {
        List<String> props = baseProperties();
        props.remove("app.kafka.defaults.consumer.concurrency=1");
        props.add("app.kafka.defaults.consumer.concurrency=0");

        contextRunner.withPropertyValues(props.toArray(String[]::new))
                .run(context -> {
                    assertThat(context).hasFailed();
                    BindValidationException failure = getBindValidationFailure(context.getStartupFailure());
                    assertHasFieldError(failure, "concurrency");
                });
    }

    @Test
    void shouldFailWhenDefaultsMaxPollRecordsNegative() {
        List<String> props = baseProperties();
        props.remove("app.kafka.defaults.consumer.max-poll-records=500");
        props.add("app.kafka.defaults.consumer.max-poll-records=-1");

        contextRunner.withPropertyValues(props.toArray(String[]::new))
                .run(context -> {
                    assertThat(context).hasFailed();
                    BindValidationException failure = getBindValidationFailure(context.getStartupFailure());
                    assertHasFieldError(failure, "maxPollRecords");
                });
    }

    @Test
    void shouldFailWhenConsumerTopicBlank() {
        List<String> props = baseProperties();
        props.remove("app.kafka.consumers.demo-message.topic=demo-topic");
        props.add("app.kafka.consumers.demo-message.topic=");

        contextRunner.withPropertyValues(props.toArray(String[]::new))
                .run(context -> {
                    assertThat(context).hasFailed();
                    BindValidationException failure = getBindValidationFailure(context.getStartupFailure());
                    assertHasFieldError(failure, "topic");
                });
    }

    private List<String> baseProperties() {
        List<String> props = new ArrayList<>();
        props.add("app.kafka.defaults.bootstrap-servers=localhost:9092");
        props.add("app.kafka.defaults.consumer.group-id=kafka-integration-group");
        props.add("app.kafka.defaults.consumer.auto-offset-reset=earliest");
        props.add("app.kafka.defaults.consumer.concurrency=1");
        props.add("app.kafka.defaults.consumer.auto-startup=true");
        props.add("app.kafka.defaults.consumer.enable-auto-commit=true");
        props.add("app.kafka.defaults.consumer.auto-commit-interval-ms=1000");
        props.add("app.kafka.defaults.consumer.max-poll-interval-ms=300000");
        props.add("app.kafka.defaults.consumer.max-poll-records=500");
        props.add("app.kafka.defaults.consumer.log-events=false");

        props.add("app.kafka.consumers.demo-message.topic=demo-topic");
        props.add("app.kafka.consumers.user-created.topic=user-created-topic");
        props.add("app.kafka.consumers.order-paid.topic=order-paid-topic");
        props.add("app.kafka.consumers.inventory-adjusted.topic=inventory-adjusted-topic");
        props.add("app.kafka.consumers.parallel-group.topic=demo-topic");
        props.add("app.kafka.legacy.alpha.topic=legacy-a-topic");
        props.add("app.kafka.legacy.alpha.group-id=legacy-a-group");
        props.add("app.kafka.legacy.alpha.concurrency=1");
        props.add("app.kafka.legacy.beta.topic=legacy-b-topic");
        props.add("app.kafka.legacy.beta.group-id=legacy-b-group");
        props.add("app.kafka.legacy.beta.concurrency=1");
        props.add("app.kafka.legacy.gamma.topic=legacy-c-topic");
        props.add("app.kafka.legacy.gamma.group-id=legacy-c-group");
        props.add("app.kafka.legacy.gamma.concurrency=1");
        return props;
    }

    private BindValidationException getBindValidationFailure(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof BindValidationException bindValidationException) {
                return bindValidationException;
            }
            current = current.getCause();
        }
        throw new AssertionError("Expected BindValidationException but got: " + failure);
    }

    private void assertHasFieldError(BindValidationException failure, String field) {
        assertThat(failure.getValidationErrors().getAllErrors())
                .filteredOn(error -> error instanceof FieldError)
                .map(error -> ((FieldError) error).getField())
                .contains(field);
    }
}
