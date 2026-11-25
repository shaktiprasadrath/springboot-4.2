package com.deploylab.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

// Episode 4: refunds.url is not set, so the default is built from payments.base-url.
@SpringBootTest(properties = "management.tracing.export.enabled=false")
@EnableConfigurationProperties(RefundProperties.class)
class DefaultValueTest {

    @Autowired
    RefundProperties refunds;

    @Test
    void defaultUsesAnotherProperty() {
        System.out.println("refunds.url = " + refunds.url());
        assertThat(refunds.url()).isEqualTo("https://payments.example.com/refunds");
    }
}
