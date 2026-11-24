package com.deploylab.orders;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.*;

@RestController
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    // Episode 3: every log line of one checkout carries the same trace id.
    @GetMapping("/orders/{id}/checkout")
    public Map<String, String> checkout(@PathVariable String id) {
        log.info("checkout started for order {}", id);
        log.info("checkout finished for order {}", id);
        return Map.of("trace_id", MDC.get("trace_id"));   // only the trace id, never the whole log context
    }
}
