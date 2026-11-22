package com.deploylab.orders;

public class PaymentDeclinedException extends RuntimeException {
    public PaymentDeclinedException(String orderId) {
        super("Payment declined for order " + orderId);
    }
}
