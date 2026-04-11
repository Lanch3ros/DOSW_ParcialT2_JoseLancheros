package edu.dosw.parcial.controller;

import edu.dosw.parcial.controller.dtos.response.ProductResponse;
import edu.dosw.parcial.core.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping("/qr/{qrCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProductResponse> getByQrCode(@PathVariable String qrCode) {
        log.info("GET /api/products/qr/{}", qrCode);
        ProductResponse response = productService.getByQrCode(qrCode);
        return ResponseEntity.ok(response);
    }
}
