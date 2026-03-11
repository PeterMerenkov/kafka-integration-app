package com.example.kafkaintegration.consumer;

import com.example.kafkaintegration.config.KafkaConsumerProperties;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerRegistrationService {

    private final List<CustomKafkaConsumer<?, ?>> consumers;
    private final ApplicationContext context;
    private final ConsumerFactory<String, String> consumerFactory;
    private final ObjectMapper objectMapper;

    private final List<ConcurrentMessageListenerContainer<String, String>> containers = new ArrayList<>();

    public void startAll() {
        for (CustomKafkaConsumer<?, ?> consumer : consumers) {
            registerConsumer(consumer);
        }
    }

    public void stopAll() {
        for (ConcurrentMessageListenerContainer<String, String> container : containers) {
            if (container.isRunning()) {
                container.stop();
            }
        }
        containers.clear();
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
            containerProperties.setAckMode(ContainerProperties.AckMode.RECORD);
        }
        containerProperties.setMessageListener((MessageListener<String, String>) record ->
                handleRecord(consumer, types.payloadType(), record));
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
            ConsumerRecord<String, String> record
    ) {
        T payload = (T) deserialize(record.value(), payloadType);
        consumer.handle(payload);
    }

    @SneakyThrows
    private Object deserialize(String value, JavaType payloadType) {
        if (payloadType.getRawClass().equals(String.class)) {
            return value;
        }
        return objectMapper.readValue(value, payloadType);
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

    private record ResolvedTypes(JavaType payloadType, Class<? extends KafkaConsumerProperties> configType) {
    }
}
