package com.deploylab.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.opentelemetry.autoconfigure.OtlpProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

// Episode 3: Spring Boot binds the common OTLP settings that traces, logs and metrics share.
@SpringBootTest(properties = "management.tracing.export.enabled=false")
@ActiveProfiles("otlp")
class OtlpConfigTest {

    @Autowired
    OtlpProperties otlp;

    @Test
    void oneEndpointForAllSignals() {
        assertThat(otlp.getEndpoint()).isEqualTo("http://otel-collector:4318");
        assertThat(otlp.getHeaders()).containsEntry("authorization", "Bearer demo-token");
        assertThat(otlp.getCompression()).isEqualTo(OtlpProperties.Compression.GZIP);
    }
}
