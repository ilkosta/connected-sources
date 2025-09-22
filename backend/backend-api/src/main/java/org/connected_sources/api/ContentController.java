package org.connected_sources.api;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/content")
public class ContentController {
    @PostMapping("/create-product")
    public String createProduct() {
        return "Product created";
    }

    @PostMapping("/withdraw")
    public String withdrawPublication() {
        return "Publication withdrawn";
    }
}