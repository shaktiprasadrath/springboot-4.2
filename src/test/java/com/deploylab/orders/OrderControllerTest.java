package com.deploylab.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class OrderControllerTest {

    @Autowired RestTestClient client;
    @MockitoBean PaymentClient payments;

    @Test
    void paysTheOrder() {
        given(payments.charge("42", 1999)).willReturn(new PaymentClient.Charge("ch_1", "PAID"));

        client.post().uri("/orders/42/pay?amountCents=1999")
              .exchange()
              .expectStatus().isOk()
              .expectBody().jsonPath("$.status").isEqualTo("PAID");
    }

    @Test
    void declinedPaymentReturns402() {
        given(payments.charge("42", 1999)).willThrow(new PaymentDeclinedException("42"));

        client.post().uri("/orders/42/pay?amountCents=1999")
              .exchange()
              .expectStatus().isEqualTo(402);
    }
}
