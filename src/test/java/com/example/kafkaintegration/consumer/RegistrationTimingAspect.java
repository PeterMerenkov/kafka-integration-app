package com.example.kafkaintegration.consumer;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Profile("timing")
@RequiredArgsConstructor
public class RegistrationTimingAspect {

    private final RegistrationTimingProbe probe;

    @Around("execution(* com.example.kafkaintegration.consumer.KafkaConsumerRegistrationService.startAll(..))")
    public Object measureStartAll(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();
        probe.markStart(start);
        try {
            return joinPoint.proceed();
        } finally {
            probe.markFinish(Instant.now());
        }
    }
}
