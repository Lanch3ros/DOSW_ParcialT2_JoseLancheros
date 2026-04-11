package edu.dosw.parcial.controller;

import edu.dosw.parcial.controller.dtos.response.ProductResponse;
import edu.dosw.parcial.core.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Productos", description = "Consulta de productos mediante código QR")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/qr/{qrCode}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consultar producto por QR", description = "Retorna la información del producto asociado al código QR escaneado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto encontrado"),
        @ApiResponse(responseCode = "400", description = "Producto no encontrado con ese código QR"),
        @ApiResponse(responseCode = "401", description = "Token JWT no proporcionado o inválido")
    })
    public ResponseEntity<ProductResponse> getByQrCode(@PathVariable String qrCode) {
        log.info("GET /api/products/qr/{}", qrCode);
        ProductResponse response = productService.getByQrCode(qrCode);
        return ResponseEntity.ok(response);
    }
}
