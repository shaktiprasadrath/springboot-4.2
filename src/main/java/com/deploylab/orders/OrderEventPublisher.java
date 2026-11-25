package com.deploylab.orders;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

// Episode 4: order events go to RabbitMQ over AMQP 0.9, now from spring-boot-starter-rabbitmq.
@Component
public class OrderEventPublisher {

    private final RabbitTemplate rabbit;

    public OrderEventPublisher(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    public void orderPaid(String orderId) {
        rabbit.convertAndSend("orders", "order.paid", orderId);
    }
}
