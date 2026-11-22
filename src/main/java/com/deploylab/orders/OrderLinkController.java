package com.deploylab.orders;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class OrderLinkController {

    // Change 1: the link the customer clicks. Behind the gateway it must start with /shop.
    @GetMapping("/orders/{id}")
    public Map<String, String> order(@PathVariable String id) {
        String self = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/orders/{id}").buildAndExpand(id).toUriString();
        return Map.of("id", id, "self", self);
    }

    // Change 2: after paying, send the customer to the order page.
    @GetMapping("/orders/{id}/receipt")
    public void receipt(@PathVariable String id, HttpServletResponse response) throws IOException {
        response.sendRedirect("/orders/" + id);
    }
}
