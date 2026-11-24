package com.deploylab.orders;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "management.tracing.mdc.trace-id-key=trace_id",
        "management.tracing.mdc.span-id-key=span_id",
        "management.tracing.sampling.probability=1.0",
        "management.tracing.export.enabled=false"})
@AutoConfigureRestTestClient
@AutoConfigureTracing
@ExtendWith(OutputCaptureExtension.class)
class TraceIdKeyTest {

    @Autowired
    RestTestClient client;

    @Test
    void everyLogLineHasTheSameTraceId(CapturedOutput output) {
        Map<String, String> mdc = client.get().uri("/orders/42/checkout").exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Map<String, String>>() {}).returnResult().getResponseBody();

        String traceId = mdc.get("trace_id");
        System.out.println("MDC: " + mdc);
        assertThat(traceId).hasSize(32);
        // the correlation id in the log output follows the new key automatically
        assertThat(output.getOut().lines().filter(l -> l.contains("checkout") && l.contains(traceId))).hasSize(2);
    }
}
