package com.deploylab.orders;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {

    private final PaymentClient payments;

    public OrderController(PaymentClient payments) {
        this.payments = payments;
    }

    @PostMapping("/orders/{id}/pay")
    public PaymentClient.Charge pay(@PathVariable String id, @RequestParam long amountCents) {
        return payments.charge(id, amountCents);
    }

    @ExceptionHandler(PaymentDeclinedException.class)
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    String declined(PaymentDeclinedException e) {
        return e.getMessage();
    }

    @ExceptionHandler(PaymentUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    String unavailable(PaymentUnavailableException e) {
        return e.getMessage();
    }
}
