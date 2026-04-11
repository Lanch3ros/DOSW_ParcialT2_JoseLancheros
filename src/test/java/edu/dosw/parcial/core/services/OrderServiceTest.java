package edu.dosw.parcial.core.services;

import edu.dosw.parcial.controller.dtos.request.CreateOrderRequest;
import edu.dosw.parcial.controller.dtos.request.OrderItemRequest;
import edu.dosw.parcial.controller.dtos.request.UpdateOrderStatusRequest;
import edu.dosw.parcial.controller.dtos.response.OrderResponse;
import edu.dosw.parcial.controller.mappers.OrderMapper;
import edu.dosw.parcial.core.models.OrderStatus;
import edu.dosw.parcial.core.models.ProductStatus;
import edu.dosw.parcial.core.models.UserRole;
import edu.dosw.parcial.core.validators.OrderStateValidator;
import edu.dosw.parcial.core.validators.StockValidator;
import edu.dosw.parcial.persistence.entities.OrderEntity;
import edu.dosw.parcial.persistence.entities.ProductEntity;
import edu.dosw.parcial.persistence.entities.UserEntity;
import edu.dosw.parcial.persistence.repositories.OrderRepository;
import edu.dosw.parcial.persistence.repositories.ProductRepository;
import edu.dosw.parcial.persistence.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private StockValidator stockValidator;
    @Mock private OrderStateValidator orderStateValidator;
    @Mock private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    // ── createOrder ─────────────────────────────────────────────────────────

    @Test
    void createOrder_success_returnsOrderResponse() {
        UserEntity user = buildUser(1L, "cliente@eci.edu.co");
        ProductEntity product = buildProduct(1L, "Café", 10, new BigDecimal("2500"));
        OrderEntity saved = buildOrder(1L, user, OrderStatus.CREADO);
        OrderResponse expected = OrderResponse.builder().id("1").status("CREADO").build();

        when(userRepository.findByEmail("cliente@eci.edu.co")).thenReturn(Optional.of(user));
        when(orderRepository.findByUser_IdAndStatusIn(eq(1L), any())).thenReturn(Optional.empty());
        when(stockValidator.validateAndGet("QR-001", 2)).thenReturn(product);
        when(productRepository.findByQrCode("QR-001")).thenReturn(Optional.of(product));
        when(orderRepository.save(any())).thenReturn(saved);
        when(orderMapper.toResponse(saved)).thenReturn(expected);

        OrderResponse result = orderService.createOrder(buildCreateRequest("QR-001", 2), "cliente@eci.edu.co");

        assertThat(result.getStatus()).isEqualTo("CREADO");
        verify(productRepository).save(argThat(p -> p.getStock() == 8));
    }

    @Test
    void createOrder_userNotFound_throwsIllegalArgument() {
        when(userRepository.findByEmail("noexiste@eci.edu.co")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(buildCreateRequest("QR-001", 1), "noexiste@eci.edu.co"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void createOrder_activeOrderExists_throwsIllegalState() {
        UserEntity user = buildUser(1L, "cliente@eci.edu.co");
        OrderEntity active = buildOrder(2L, user, OrderStatus.CREADO);

        when(userRepository.findByEmail("cliente@eci.edu.co")).thenReturn(Optional.of(user));
        when(orderRepository.findByUser_IdAndStatusIn(eq(1L), any())).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> orderService.createOrder(buildCreateRequest("QR-001", 1), "cliente@eci.edu.co"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pedido activo");
    }

    // ── cancelOrder ─────────────────────────────────────────────────────────

    @Test
    void cancelOrder_success_returnsOrderWithCanceladoStatus() {
        UserEntity user = buildUser(1L, "cliente@eci.edu.co");
        OrderEntity order = buildOrder(1L, user, OrderStatus.CREADO);
        OrderResponse expected = OrderResponse.builder().id("1").status("CANCELADO").build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expected);

        OrderResponse result = orderService.cancelOrder(1L, "cliente@eci.edu.co");

        assertThat(result.getStatus()).isEqualTo("CANCELADO");
        verify(orderStateValidator).validateCancellation(order, "cliente@eci.edu.co");
    }

    @Test
    void cancelOrder_orderNotFound_throwsIllegalArgument() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(99L, "cliente@eci.edu.co"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pedido no encontrado");
    }

    // ── updateOrderStatus ───────────────────────────────────────────────────

    @Test
    void updateOrderStatus_creadoToEnPreparacion_success() {
        UserEntity user = buildUser(1L, "admin@eci.edu.co");
        OrderEntity order = buildOrder(1L, user, OrderStatus.CREADO);
        OrderResponse expected = OrderResponse.builder().id("1").status("EN_PREPARACION").build();
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus("EN_PREPARACION");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expected);

        OrderResponse result = orderService.updateOrderStatus(1L, request);

        assertThat(result.getStatus()).isEqualTo("EN_PREPARACION");
        verify(orderStateValidator).validateAdminTransition(order, OrderStatus.EN_PREPARACION);
    }

    @Test
    void updateOrderStatus_orderNotFound_throwsIllegalArgument() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus("EN_PREPARACION");
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(99L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pedido no encontrado");
    }

    @Test
    void updateOrderStatus_invalidTransition_throwsIllegalState() {
        UserEntity user = buildUser(1L, "admin@eci.edu.co");
        OrderEntity order = buildOrder(1L, user, OrderStatus.CANCELADO);
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus("EN_PREPARACION");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doThrow(new IllegalStateException("Transición inválida"))
                .when(orderStateValidator).validateAdminTransition(order, OrderStatus.EN_PREPARACION);

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transición inválida");
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private UserEntity buildUser(Long id, String email) {
        return UserEntity.builder()
                .id(id).email(email).role(UserRole.ROLE_CLIENT).build();
    }

    private ProductEntity buildProduct(Long id, String name, int stock, BigDecimal price) {
        return ProductEntity.builder()
                .id(id).name(name).qrCode("QR-001")
                .stock(stock).price(price).status(ProductStatus.AVAILABLE).build();
    }

    private OrderEntity buildOrder(Long id, UserEntity user, OrderStatus status) {
        return OrderEntity.builder()
                .id(id).user(user).status(status)
                .total(new BigDecimal("5000")).build();
    }

    private CreateOrderRequest buildCreateRequest(String qrCode, int quantity) {
        OrderItemRequest item = new OrderItemRequest();
        item.setQrCode(qrCode);
        item.setQuantity(quantity);
        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(List.of(item));
        return request;
    }
}
