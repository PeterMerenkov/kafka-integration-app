package com.example.kafkaintegration.consumer;

import com.example.kafkaintegration.config.KafkaConsumerProperties;
import com.example.kafkaintegration.config.KafkaDefaultProps;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerRegistrationService {

    private final List<CustomKafkaConsumer<?, ?>> consumers;
    private final ApplicationContext context;
    private final ObjectMapper objectMapper;
    private final KafkaDefaultProps defaultProps;

    private final Logger EVENTS_LOGGER = LoggerFactory.getLogger("kafka-events");

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
        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(buildConsumerFactoryConfig(config),
                        new ErrorHandlingDeserializer<>(new StringDeserializer()),
                        new ErrorHandlingDeserializer<>(new StringDeserializer())); // TODO LoggingProxyJsonDeserializer
        ConcurrentMessageListenerContainer<String, String> container =
                new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);
        container.setBeanName("dynamic-kafka-consumer-" + targetClass.getSimpleName());
        container.setConcurrency(config.getConcurrency());
        container.setAutoStartup(config.isAutoStartup());
        container.setCommonErrorHandler(new CommonLoggingErrorHandler());
        container.setRecordInterceptor(new RecordInterceptor<>() {
            @Override
            public ConsumerRecord<String, String> intercept(ConsumerRecord<String, String> record, Consumer<String, String> consumer) {
                EVENTS_LOGGER.debug("Incoming: {}", record.value()); // TODO LogHelper
                return record;
            }

            @Override
            public void failure(ConsumerRecord<String, String> record, Exception exception, Consumer<String, String> consumer) {
                log.error("Error consuming event: record {}", record, exception); // TODO LogHelper
            }
        });
        containers.add(container);
        return container;
    }

    private void startContainerIfNeeded(ConcurrentMessageListenerContainer<String, String> container, KafkaConsumerProperties config) {
        if (config.isAutoStartup()) {
            container.start();
        }
    }

    private Map<String, Object> buildConsumerFactoryConfig(KafkaConsumerProperties config) {
        Map<String, Object> consumerFactoryConfig = new HashMap<>();
        consumerFactoryConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                String.join(",", defaultProps.getBootstrapServers()));
        consumerFactoryConfig.put(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
        consumerFactoryConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, config.isEnableAutoCommit());
        consumerFactoryConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getAutoOffsetReset());
        consumerFactoryConfig.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, config.getAutoCommitIntervalMs());
        consumerFactoryConfig.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, config.getMaxPollIntervalMs());
        consumerFactoryConfig.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, config.getMaxPollRecords());

        consumerFactoryConfig.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
                RoundRobinAssignor.class.getName());

        putSslProps(consumerFactoryConfig);
        return consumerFactoryConfig;
    }

    private void putSslProps(Map<String, Object> consumerFactoryConfig) {
        KafkaDefaultProps.SslProps ssl = defaultProps.getSsl();
        if (ssl == null) {
            return;
        }
        consumerFactoryConfig.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, ssl.getSecurityProtocol());
        consumerFactoryConfig.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");

        consumerFactoryConfig.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, ssl.getKeystoreType());
        consumerFactoryConfig.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, ssl.getKeystoreLocation());
        consumerFactoryConfig.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, ssl.getKeystorePassword());

        consumerFactoryConfig.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, ssl.getTruststoreType());
        consumerFactoryConfig.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, ssl.getTruststoreLocation());
        consumerFactoryConfig.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, ssl.getTruststorePassword());
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
