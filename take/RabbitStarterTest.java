package com.deploylab.orders;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.rabbitmq.autoconfigure.RabbitProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

// Episode 4: spring-boot-starter-rabbitmq keeps AMQP 0.9 and the same spring.rabbitmq.* properties. No broker needed: nothing connects.
@SpringBootTest(properties = {
        "spring.rabbitmq.host=rabbit.internal",
        "spring.rabbitmq.port=5673",
        "management.tracing.export.enabled=false"})
class RabbitStarterTest {

    @Autowired
    RabbitProperties rabbit;

    @Autowired
    ConnectionFactory connectionFactory;

    @Test
    void sameRabbitPropertiesNewPackage() {
        System.out.println("RabbitProperties is in " + RabbitProperties.class.getPackageName());
        assertThat(RabbitProperties.class.getPackageName()).isEqualTo("org.springframework.boot.rabbitmq.autoconfigure");
        assertThat(connectionFactory.getHost()).isEqualTo("rabbit.internal");
        assertThat(connectionFactory.getPort()).isEqualTo(5673);
    }
}
