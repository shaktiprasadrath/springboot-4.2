package com.deploylab.orders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {

    @Bean
    RestClientCustomizer paymentAuth(@Value("${payments.api-key}") String apiKey) {
        return builder -> builder.defaultHeader("Authorization", "Bearer " + apiKey);
    }
}
