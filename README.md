# Kafka Integration App

Spring Boot приложение с базовой интеграцией Kafka: REST endpoint для отправки сообщений и Kafka consumer для чтения.

## Требования
- Java 17+
- Maven 3.9+
- Docker

## Запуск
1. Поднять Kafka: `docker compose up -d`
2. Запустить приложение: `mvn spring-boot:run`
3. Отправить сообщение в Kafka:
   `curl -X POST http://localhost:8080/api/messages -H "Content-Type: application/json" -d "{\"key\":\"order-1\",\"message\":\"hello kafka\"}"`