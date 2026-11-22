package com.deploylab.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(PaymentClient.class)
@Import(PaymentConfig.class)
class PaymentClientTest {

    @Autowired
    PaymentClient client;
    @Autowired
    MockRestServiceServer server;

    @Test
    void chargesTheOrderWithAuthHeader() {
        server.expect(requestTo("https://payments.example.com/charges/42"))
                .andExpect(header("Authorization", "Bearer demo-key"))
                .andRespond(withSuccess("{\"id\":\"ch_1\",\"status\":\"PAID\"}", MediaType.APPLICATION_JSON));

        assertThat(client.charge("42", 1999).status()).isEqualTo("PAID");
    }

    @Test
    void declinedCardThrowsDeclined() {
        server.expect(requestTo("https://payments.example.com/charges/42"))
                .andRespond(withStatus(HttpStatus.PAYMENT_REQUIRED));

        assertThatThrownBy(() -> client.charge("42", 1999))
                .isInstanceOf(PaymentDeclinedException.class);
    }

    @Test
    void serverErrorThrowsUnavailable() {
        server.expect(requestTo("https://payments.example.com/charges/42"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> client.charge("42", 1999))
                .isInstanceOf(PaymentUnavailableException.class);
    }
}
