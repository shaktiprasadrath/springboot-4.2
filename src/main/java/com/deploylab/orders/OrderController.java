package com.deploylab.orders;

import io.micrometer.core.instrument.Metrics;
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
        PaymentClient.Charge charge = payments.charge(id, amountCents);
        Metrics.counter("orders.paid").increment();   // Change 7: global registry
        return charge;
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
