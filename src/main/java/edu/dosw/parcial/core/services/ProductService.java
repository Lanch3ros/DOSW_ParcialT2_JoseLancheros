package edu.dosw.parcial.core.services;

import edu.dosw.parcial.controller.dtos.response.ProductResponse;
import edu.dosw.parcial.controller.mappers.ProductMapper;
import edu.dosw.parcial.persistence.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponse getByQrCode(String qrCode) {
        log.info("Consultando producto con QR: {}", qrCode);

        return productRepository.findByQrCode(qrCode)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto no encontrado con código QR: " + qrCode));
    }
}
