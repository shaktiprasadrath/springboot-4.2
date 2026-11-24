package com.deploylab.orders;

import java.util.Set;
import java.util.TreeSet;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

class ConventionsTest {

    static Set<String> httpMeters(RestTestClient client, MeterRegistry registry) {
        client.get().uri("/orders/42").exchange().expectStatus().isOk();
        Set<String> names = new TreeSet<>();
        for (Meter m : registry.getMeters()) {
            if (m.getId().getName().startsWith("http.server")) names.add(m.getId().getName());
        }
        System.out.println("HTTP meters: " + names);
        return names;
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @AutoConfigureRestTestClient
    @AutoConfigureMetrics
    class Default42 {
        @Autowired
        RestTestClient client;
        @Autowired
        MeterRegistry registry;

        @Test
        void defaultNames() {
            assertThat(httpMeters(client, registry)).contains("http.server.requests");
        }
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            properties = "management.observations.conventions=open-telemetry")
    @AutoConfigureRestTestClient
    @AutoConfigureMetrics
    class OpenTelemetryConventions {
        @Autowired
        RestTestClient client;
        @Autowired
        MeterRegistry registry;

        @Test
        void openTelemetryNames() {
            assertThat(httpMeters(client, registry)).isNotEmpty().doesNotContain("http.server.requests");
        }
    }
}
