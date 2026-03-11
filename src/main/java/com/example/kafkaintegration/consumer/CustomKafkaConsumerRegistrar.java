package com.example.kafkaintegration.consumer;

import com.example.kafkaintegration.config.KafkaConsumerProperties;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.ResolvableType;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.consumers-enabled", havingValue = "true", matchIfMissing = true)
public class CustomKafkaConsumerRegistrar implements SmartLifecycle {

    private final List<CustomKafkaConsumer<?, ?>> consumers;
    private final ApplicationContext context;
    private final ConsumerFactory<String, String> consumerFactory;
    private final ObjectMapper objectMapper;

    private final List<ConcurrentMessageListenerContainer<String, String>> containers = new ArrayList<>();
    private volatile boolean running;

    @Override
    public void start() {
        if (running) {
            return;
        }
        registerAllConsumers();
        running = true;
    }

    private void registerAllConsumers() {
        for (CustomKafkaConsumer<?, ?> consumer : consumers) {
            registerConsumer(consumer);
        }
    }

    private void registerConsumer(CustomKafkaConsumer<?, ?> consumer) {
        Class<?> targetClass = AopUtils.getTargetClass(consumer);
        ResolvedTypes types = resolveTypes(targetClass);
        KafkaConsumerProperties config = context.getBean(types.configType());
        ContainerProperties containerProperties = buildContainerProperties(consumer, types, config);
        ConcurrentMessageListenerContainer<String, String> container = createContainer(targetClass, config, containerProperties);
        startContainerIfNeeded(container, config);
    }

    private ContainerProperties buildContainerProperties(
            CustomKafkaConsumer<?, ?> consumer,
            ResolvedTypes types,
            KafkaConsumerProperties config
    ) {
        ContainerProperties containerProperties = new ContainerProperties(config.getTopic());
        containerProperties.setGroupId(config.getGroupId());
        containerProperties.setKafkaConsumerProperties(kafkaOverrides(config));
        if (!config.isEnableAutoCommit()) {
            containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL);
        }
        containerProperties.setMessageListener((AcknowledgingMessageListener<String, String>) (record, acknowledgment) ->
                handleRecord(consumer, types.payloadType(), config, record, acknowledgment));
        return containerProperties;
    }

    private ConcurrentMessageListenerContainer<String, String> createContainer(
            Class<?> targetClass,
            KafkaConsumerProperties config,
            ContainerProperties containerProperties
    ) {
        ConcurrentMessageListenerContainer<String, String> container =
                new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);
        container.setBeanName("dynamic-kafka-consumer-" + targetClass.getSimpleName());
        container.setConcurrency(config.getConcurrency());
        container.setAutoStartup(config.isAutoStartup());
        containers.add(container);
        return container;
    }

    private void startContainerIfNeeded(ConcurrentMessageListenerContainer<String, String> container, KafkaConsumerProperties config) {
        if (config.isAutoStartup()) {
            container.start();
        }
    }

    private Properties kafkaOverrides(KafkaConsumerProperties config) {
        Properties overrides = new Properties();
        overrides.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getAutoOffsetReset());
        overrides.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.toString(config.isEnableAutoCommit()));
        overrides.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, Integer.toString(config.getAutoCommitIntervalMs()));
        overrides.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, Integer.toString(config.getMaxPollIntervalMs()));
        overrides.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, Integer.toString(config.getMaxPollRecords()));
        return overrides;
    }

    @SuppressWarnings("unchecked")
    private <T> void handleRecord(
            CustomKafkaConsumer<T, ?> consumer,
            JavaType payloadType,
            KafkaConsumerProperties config,
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment
    ) {
        try {
            T payload = (T) deserialize(record.value(), payloadType);
            if (config.isLogEvents()) {
                logEvent(record, payload);
            }
            consumer.handle(payload);
            if (!config.isEnableAutoCommit() && acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (Exception e) {
            logProcessingError(consumer, record, e);
        }
    }

    private Object deserialize(String value, JavaType payloadType) throws Exception {
        if (payloadType.getRawClass().equals(String.class)) {
            return value;
        }
        return objectMapper.readValue(value, payloadType);
    }

    private void logEvent(ConsumerRecord<String, String> record, Object payload) {
        String preview = payload == null ? "null" : payload.toString();
        if (preview.length() > 200) {
            preview = preview.substring(0, 200) + "...";
        }
        log.info(
                "Kafka event topic='{}' partition={} offset={} key='{}' payload='{}'",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                preview
        );
    }

    private void logProcessingError(CustomKafkaConsumer<?, ?> consumer, ConsumerRecord<String, String> record, Exception e) {
        log.error(
                "Consumer '{}' failed for topic='{}' partition={} offset={} key='{}'.",
                consumer.getClass().getSimpleName(),
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                e
        );
    }

    private ResolvedTypes resolveTypes(Class<?> targetClass) {
        ResolvableType type = ResolvableType.forClass(targetClass).as(CustomKafkaConsumer.class);
        ResolvableType payloadType = type.getGeneric(0);
        Class<?> payloadClass = payloadType.resolve();
        Class<?> configClass = type.getGeneric(1).resolve();

        if (payloadClass == null || payloadClass.equals(Object.class)) {
            throw new BeanCreationException("Cannot resolve payload generic type for consumer '" + targetClass.getName() + "'.");
        }
        if (configClass == null || configClass.equals(Object.class)) {
            throw new BeanCreationException("Cannot resolve config generic type for consumer '" + targetClass.getName() + "'.");
        }

        JavaType payloadJavaType = objectMapper.getTypeFactory().constructType(payloadType.getType());
        return new ResolvedTypes(payloadJavaType, (Class<? extends KafkaConsumerProperties>) configClass);
    }

    @Override
    public void stop() {
        for (ConcurrentMessageListenerContainer<String, String> container : containers) {
            if (container.isRunning()) {
                container.stop();
            }
        }
        containers.clear();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private record ResolvedTypes(JavaType payloadType, Class<? extends KafkaConsumerProperties> configType) {
    }
}
