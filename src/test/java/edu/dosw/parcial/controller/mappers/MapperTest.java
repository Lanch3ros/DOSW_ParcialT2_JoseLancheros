package edu.dosw.parcial.controller.mappers;

import edu.dosw.parcial.controller.dtos.response.OrderItemResponse;
import edu.dosw.parcial.controller.dtos.response.OrderResponse;
import edu.dosw.parcial.controller.dtos.response.ProductResponse;
import edu.dosw.parcial.controller.dtos.response.UserResponse;
import edu.dosw.parcial.core.models.OrderStatus;
import edu.dosw.parcial.core.models.ProductStatus;
import edu.dosw.parcial.core.models.UserRole;
import edu.dosw.parcial.persistence.entities.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        UserMapperImpl.class,
        ProductMapperImpl.class,
        OrderItemMapperImpl.class,
        OrderMapperImpl.class
})
class MapperTest {

    @Autowired UserMapper userMapper;
    @Autowired ProductMapper productMapper;
    @Autowired OrderItemMapper orderItemMapper;
    @Autowired OrderMapper orderMapper;

    // ── UserMapper ───────────────────────────────────────────────────────────

    @Test
    void userMapper_mapsAllFields() {
        UserEntity entity = UserEntity.builder()
                .id(1L).fullName("Jose Lancheros").email("jose@eci.edu.co")
                .role(UserRole.ROLE_CLIENT).createdAt(LocalDateTime.now())
                .build();

        UserResponse response = userMapper.toResponse(entity);

        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getFullName()).isEqualTo("Jose Lancheros");
        assertThat(response.getEmail()).isEqualTo("jose@eci.edu.co");
        assertThat(response.getRole()).isEqualTo("ROLE_CLIENT");
    }

    // ── ProductMapper ────────────────────────────────────────────────────────

    @Test
    void productMapper_mapsAllFields() {
        ProductEntity entity = ProductEntity.builder()
                .id(2L).name("Café").description("Café negro")
                .price(new BigDecimal("2500")).qrCode("QR-001")
                .stock(10).status(ProductStatus.AVAILABLE)
                .build();

        ProductResponse response = productMapper.toResponse(entity);

        assertThat(response.getId()).isEqualTo("2");
        assertThat(response.getName()).isEqualTo("Café");
        assertThat(response.getQrCode()).isEqualTo("QR-001");
        assertThat(response.getStatus()).isEqualTo("AVAILABLE");
        assertThat(response.getStock()).isEqualTo(10);
    }

    // ── OrderItemMapper ──────────────────────────────────────────────────────

    @Test
    void orderItemMapper_mapsAllFields() {
        OrderItemEntity entity = OrderItemEntity.builder()
                .productId(3L).productName("Empanada")
                .quantity(2).unitPrice(new BigDecimal("1500"))
                .subtotal(new BigDecimal("3000"))
                .build();

        OrderItemResponse response = orderItemMapper.toResponse(entity);

        assertThat(response.getProductId()).isEqualTo("3");
        assertThat(response.getProductName()).isEqualTo("Empanada");
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getSubtotal()).isEqualByComparingTo("3000");
    }

    // ── OrderMapper ──────────────────────────────────────────────────────────

    @Test
    void orderMapper_mapsAllFields() {
        UserEntity user = UserEntity.builder()
                .id(1L).email("jose@eci.edu.co").role(UserRole.ROLE_CLIENT).build();

        OrderItemEntity item = OrderItemEntity.builder()
                .productId(2L).productName("Café").quantity(1)
                .unitPrice(new BigDecimal("2500")).subtotal(new BigDecimal("2500"))
                .build();

        OrderEntity entity = OrderEntity.builder()
                .id(5L).user(user).status(OrderStatus.CREADO)
                .total(new BigDecimal("2500")).items(List.of(item))
                .build();

        OrderResponse response = orderMapper.toResponse(entity);

        assertThat(response.getId()).isEqualTo("5");
        assertThat(response.getUserId()).isEqualTo("1");
        assertThat(response.getStatus()).isEqualTo("CREADO");
        assertThat(response.getTotal()).isEqualByComparingTo("2500");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getProductName()).isEqualTo("Café");
    }
}
