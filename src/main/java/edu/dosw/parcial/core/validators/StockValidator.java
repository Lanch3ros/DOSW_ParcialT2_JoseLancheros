package edu.dosw.parcial.core.validators;

import edu.dosw.parcial.core.models.ProductStatus;
import edu.dosw.parcial.persistence.entities.ProductEntity;
import edu.dosw.parcial.persistence.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockValidator {

    private final ProductRepository productRepository;

    public ProductEntity validateAndGet(String qrCode, Integer requestedQuantity) {
        log.debug("Validando stock para QR: {} cantidad: {}", qrCode, requestedQuantity);

        ProductEntity product = productRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto no encontrado con código QR: " + qrCode));

        if (product.getStatus() == ProductStatus.UNAVAILABLE) {
            throw new IllegalArgumentException(
                    "Producto no disponible: " + product.getName());
        }

        if (product.getStock() < requestedQuantity) {
            throw new IllegalStateException(
                    "Stock insuficiente para '" + product.getName() +
                            "'. Disponible: " + product.getStock() +
                            ", solicitado: " + requestedQuantity);
        }

        return product;
    }
}