package com.example.kafkaintegration.controller;

import com.example.kafkaintegration.dto.PublishRequest;
import com.example.kafkaintegration.service.KafkaProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final KafkaProducerService kafkaProducerService;

    @PostMapping
    public ResponseEntity<String> publish(@Valid @RequestBody PublishRequest request) {
        kafkaProducerService.send(request.key(), request.payload().toString());
        return ResponseEntity.accepted().body("Message sent to Kafka");
    }
}
