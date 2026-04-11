package edu.dosw.parcial.core.validators;

import edu.dosw.parcial.core.models.ProductStatus;
import edu.dosw.parcial.persistence.entities.ProductEntity;
import edu.dosw.parcial.persistence.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockValidatorTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private StockValidator stockValidator;

    @Test
    void validateAndGet_productNotFound_throwsIllegalArgument() {
        when(productRepository.findByQrCode("QR-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockValidator.validateAndGet("QR-999", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Producto no encontrado");
    }

    @Test
    void validateAndGet_productUnavailable_throwsIllegalArgument() {
        ProductEntity product = buildProduct("Café", 10, ProductStatus.UNAVAILABLE);
        when(productRepository.findByQrCode("QR-001")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> stockValidator.validateAndGet("QR-001", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Producto no disponible");
    }

    @Test
    void validateAndGet_insufficientStock_throwsIllegalState() {
        ProductEntity product = buildProduct("Café", 2, ProductStatus.AVAILABLE);
        when(productRepository.findByQrCode("QR-001")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> stockValidator.validateAndGet("QR-001", 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void validateAndGet_validProduct_returnsProduct() {
        ProductEntity product = buildProduct("Café", 10, ProductStatus.AVAILABLE);
        when(productRepository.findByQrCode("QR-001")).thenReturn(Optional.of(product));

        ProductEntity result = stockValidator.validateAndGet("QR-001", 3);

        assertThat(result).isEqualTo(product);
    }

    @Test
    void validateAndGet_exactStock_returnsProduct() {
        ProductEntity product = buildProduct("Café", 3, ProductStatus.AVAILABLE);
        when(productRepository.findByQrCode("QR-001")).thenReturn(Optional.of(product));

        ProductEntity result = stockValidator.validateAndGet("QR-001", 3);

        assertThat(result.getStock()).isEqualTo(3);
    }

    private ProductEntity buildProduct(String name, int stock, ProductStatus status) {
        return ProductEntity.builder()
                .id(1L)
                .name(name)
                .price(new BigDecimal("2500"))
                .qrCode("QR-001")
                .stock(stock)
                .status(status)
                .build();
    }
}
