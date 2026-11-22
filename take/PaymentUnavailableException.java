package com.deploylab.orders;

public class PaymentUnavailableException extends RuntimeException {
    public PaymentUnavailableException(int status) {
        super("Payment API unavailable: HTTP " + status);
    }
}
