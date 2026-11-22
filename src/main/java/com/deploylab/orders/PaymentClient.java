package com.deploylab.orders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PaymentClient {

    private final RestClient rest;

    public PaymentClient(RestClient.Builder builder, @Value("${payments.base-url}") String baseUrl) {
        this.rest = builder.baseUrl(baseUrl).build();
    }

    public Charge charge(String orderId, long amountCents) {
        return rest.post()
                .uri("/charges/{id}", orderId)
                .body(new ChargeRequest(amountCents))
                .retrieve()
                .onStatus(s -> s.value() == 402, (req, res) -> {
                    throw new PaymentDeclinedException(orderId);
                })
                .onStatus(s -> s.is5xxServerError(), (req, res) -> {
                    throw new PaymentUnavailableException(res.getStatusCode().value());
                })
                .body(Charge.class);
    }

    public record ChargeRequest(long amountCents) {}

    public record Charge(String id, String status) {}
}
