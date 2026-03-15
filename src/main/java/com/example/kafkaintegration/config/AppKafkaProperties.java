package com.example.kafkaintegration.config;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.kafka")
public class AppKafkaProperties {

    @NotEmpty
    private List<String> bootstrapServers;

    private SslProperties ssl = new SslProperties();

    @Getter
    @Setter
    public static class SslProperties {
        private String securityProtocol;
        private String keystoreType;
        private String keystoreLocation;
        private String keystorePassword;
        private String truststoreType;
        private String truststoreLocation;
        private String truststorePassword;
    }
}
