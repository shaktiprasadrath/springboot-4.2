package com.deploylab.orders;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

class InvoiceUploadTest {

    static final int TWO_MB = 2 * 1024 * 1024;

    static WebTestClient.ResponseSpec upload(WebTestClient client) {
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("file", new ByteArrayResource(new byte[TWO_MB]) {
            @Override
            public String getFilename() { return "invoice.pdf"; }
        });
        return client.post().uri("/orders/42/invoice")
                .body(BodyInserters.fromMultipartData(body.build()))
                .exchange();
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            properties = "spring.main.web-application-type=reactive")
    @AutoConfigureWebTestClient
    class Default42 {
        @Autowired
        WebTestClient client;

        @Test
        void twoMegabyteInvoiceFails() {
            // the 1MB limit throws DataBufferLimitException, which WebFlux answers with a 500
            upload(client).expectStatus().is5xxServerError();
        }
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            properties = {"spring.main.web-application-type=reactive",
                    "spring.webflux.multipart.max-disk-usage-per-part=10MB"})
    @AutoConfigureWebTestClient
    class RaisedLimit {
        @Autowired
        WebTestClient client;

        @Test
        void twoMegabyteInvoiceIsStored() {
            upload(client).expectStatus().isOk()
                    .expectBody(String.class).isEqualTo("stored " + TWO_MB + " bytes for order 42");
        }
    }
}
