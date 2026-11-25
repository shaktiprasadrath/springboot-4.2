package com.deploylab.orders;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

// Episode 4: @DefaultValue can now use a property placeholder, resolved during binding.
@ConfigurationProperties("refunds")
public record RefundProperties(@DefaultValue("${payments.base-url}/refunds") String url) {
}
