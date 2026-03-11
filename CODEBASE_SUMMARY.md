# Kafka Integration App — Codebase Summary

## Overview
This is a Spring Boot 3.3.x application that demonstrates Kafka integration. It provides:
- A REST endpoint to publish messages to Kafka.
- A dynamic consumer registration mechanism that discovers `CustomKafkaConsumer` beans and creates Kafka listener containers for each one.
- Configuration-driven consumer settings with defaults and per-consumer overrides.

## Architecture At A Glance
- **Producer path**: HTTP POST `/api/messages` -> `KafkaProducerService` -> Kafka topic from `app.kafka.producer-topic`.
- **Consumer path**: `CustomKafkaConsumerRegistrar` scans all `CustomKafkaConsumer<?, ?>` beans -> resolves their payload type and config class -> creates `ConcurrentMessageListenerContainer` per consumer.
- **Config**: `app.kafka` in `application.yml` provides defaults + per-consumer settings.

## Key Components
- **Entry point**
  - `src/main/java/com/example/kafkaintegration/KafkaIntegrationApplication.java`
- **Configuration**
  - `src/main/java/com/example/kafkaintegration/config/KafkaAppProperties.java`
    - Binds `app.kafka.*`, validates required fields, merges defaults with per-consumer overrides via `resolveConsumer`.
  - `src/main/java/com/example/kafkaintegration/config/*ConsumerConfig.java`
    - One per consumer type; resolves a `ResolvedConsumer` from `KafkaAppProperties` and exposes it via `KafkaConsumerProperties`.
  - `src/main/java/com/example/kafkaintegration/config/KafkaConsumerProperties.java`
    - Interface used by the registrar to read consumer settings.
  - `src/main/java/com/example/kafkaintegration/config/AppConfig.java`
    - Enables configuration properties binding.
- **Consumer framework**
  - `src/main/java/com/example/kafkaintegration/consumer/CustomKafkaConsumer.java`
    - Generic interface: `handle(T message)`.
  - `src/main/java/com/example/kafkaintegration/consumer/CustomKafkaConsumerRegistrar.java`
    - Discovers all `CustomKafkaConsumer` beans, resolves payload/config generics, and starts one `ConcurrentMessageListenerContainer` per consumer.
    - Handles deserialization (String or JSON -> DTO via Jackson), optional event logging, and manual ack.
- **Concrete consumers**
  - `KafkaMessageListener`, `UserCreatedConsumer`, `OrderPaidConsumer`, `InventoryAdjustedConsumer`
  - Each logs the received DTO.
- **Producer + controller**
  - `src/main/java/com/example/kafkaintegration/controller/MessageController.java`
    - POST `/api/messages` with `PublishRequest`.
  - `src/main/java/com/example/kafkaintegration/service/KafkaProducerService.java`
    - Sends to the configured producer topic.
- **DTOs**
  - `PublishRequest` uses a `JsonNode` payload.
  - `DemoMessageDto`, `UserCreatedDto`, `OrderPaidDto`, `InventoryAdjustedDto` define sample payloads with validation annotations.

## Configuration Defaults And Overrides
Defined in `src/main/resources/application.yml` under `app.kafka`:
- `producer-topic`: where the REST endpoint publishes.
- `defaults`: default consumer settings (group ID, offset reset, concurrency, auto-start, auto-commit, poll settings, and logging flag).
- `consumers`: per-consumer overrides (topics and optional overrides).

The registrar reads:
- Topic, group, offset reset, concurrency, auto-startup, auto-commit, commit interval, max poll interval, max poll records, and log-events.

## Runtime Behavior
- On startup, `CustomKafkaConsumerRegistrar` creates one container per `CustomKafkaConsumer` bean.
- If `app.kafka.consumers-enabled=false`, the registrar is skipped.
- If `enable-auto-commit=false`, the listener switches to manual ack.

## Tests
- `KafkaIntegrationApplicationTests`: context loads.
- `KafkaAppPropertiesResolveConsumerTest`: default + override resolution.
- `KafkaAppPropertiesValidationTest`: validation errors for missing/invalid settings.
- `CustomKafkaConsumerRegistrarIntegrationTest`: routes messages to correct consumers, tolerates bad JSON.
- `CustomKafkaConsumerManualCommitIntegrationTest`: manual commit path.
- `CustomKafkaConsumerLogEventsIntegrationTest`: log-event output when enabled.

## How To Run (From README)
- Start Kafka via Docker Compose.
- Run the app with Maven.
- Publish via REST to `/api/messages`.

## What To Tweak Most Often
- `application.yml` for topics and consumer properties.
- Add new consumer:
  1. Add config entry under `app.kafka.consumers.<key>`.
  2. Create a `<Key>ConsumerConfig` that resolves the key.
  3. Implement `CustomKafkaConsumer<YourDto, YourConfig>`.
  4. Add DTO and adjust producer if needed.
