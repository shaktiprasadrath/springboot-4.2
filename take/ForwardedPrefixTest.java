package com.deploylab.orders;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

class ForwardedPrefixTest {

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            properties = "server.request.header.forwarded.use-prefix=false")
    @AutoConfigureRestTestClient
    class Default42 {
        @Autowired
        RestTestClient client;

        @Test
        void prefixIsIgnored() {
            client.get().uri("/orders/42").header("X-Forwarded-Prefix", "/shop")
                    .exchange()
                    .expectBody().jsonPath("$.self").value(String.class,
                            self -> org.assertj.core.api.Assertions.assertThat(self).doesNotContain("/shop"));
        }
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @AutoConfigureRestTestClient
    class WithUsePrefix {
        @Autowired
        RestTestClient client;

        @Test
        void linkKeepsShop() {
            client.get().uri("/orders/42").header("X-Forwarded-Prefix", "/shop")
                    .exchange()
                    .expectBody().jsonPath("$.self").value(String.class,
                            self -> org.assertj.core.api.Assertions.assertThat(self).endsWith("/shop/orders/42"));
        }
    }
}
