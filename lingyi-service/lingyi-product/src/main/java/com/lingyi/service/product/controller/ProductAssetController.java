package com.lingyi.service.product.controller;

import java.io.IOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product/assets")
public class ProductAssetController {

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getAsset(@PathVariable String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource("seed-assets/" + fileName);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/svg+xml"))
                .contentLength(resource.contentLength())
                .body(resource);
    }
}