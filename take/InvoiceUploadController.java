package com.deploylab.orders;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

// Change 4: only active when the app runs on WebFlux.
@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class InvoiceUploadController {

    @PostMapping("/orders/{id}/invoice")
    public Mono<String> upload(@PathVariable String id, @RequestPart("file") FilePart file) {
        return DataBufferUtils.join(file.content())
                .map(buffer -> {
                    int size = buffer.readableByteCount();
                    DataBufferUtils.release(buffer);
                    return "stored " + size + " bytes for order " + id;
                });
    }
}
