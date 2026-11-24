package com.deploylab.orders;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

class RelativeRedirectTest {

    static String location(int port) throws Exception {
        HttpResponse<Void> r = HttpClient.newHttpClient().send(   // does not follow redirects
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/orders/42/receipt")).build(),
                HttpResponse.BodyHandlers.discarding());
        return r.headers().firstValue("Location").orElseThrow();
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    class Default42 {
        @LocalServerPort
        int port;

        @Test
        void redirectIsRelative() throws Exception {
            assertThat(location(port)).isEqualTo("/orders/42");
        }
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            properties = "server.tomcat.servlet.use-relative-redirects=false")
    class OldBehaviour {
        @LocalServerPort
        int port;

        @Test
        void redirectIsFullUrl() throws Exception {
            assertThat(location(port)).isEqualTo("http://localhost:" + port + "/orders/42");
        }
    }
}
