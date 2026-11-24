package com.deploylab.orders;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class PaidMetricTest {

    static void pay(RestTestClient client, PaymentClient payments) {
        given(payments.charge("42", 1999)).willReturn(new PaymentClient.Charge("ch_1", "PAID"));
        client.post().uri("/orders/42/pay?amountCents=1999").exchange().expectStatus().isOk();
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @AutoConfigureRestTestClient
    @AutoConfigureMetrics
    class Default42 {
        @Autowired RestTestClient client;
        @Autowired MeterRegistry registry;
        @MockitoBean PaymentClient payments;

        @Test
        void counterIsNotInTheTestRegistry() {
            pay(client, payments);
            assertThat(registry.find("orders.paid").counter()).isNull();
        }
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @AutoConfigureRestTestClient
    @AutoConfigureMetrics(useGlobalRegistry = true)
    class GlobalRegistry {
        @Autowired RestTestClient client;
        @Autowired MeterRegistry registry;
        @MockitoBean PaymentClient payments;

        @Test
        void counterIsCounted() {
            pay(client, payments);
            assertThat(registry.get("orders.paid").counter().count()).isEqualTo(1.0);
        }
    }
}
