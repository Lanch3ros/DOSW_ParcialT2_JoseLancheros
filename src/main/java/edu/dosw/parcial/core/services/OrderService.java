package edu.dosw.parcial.core.services;

import edu.dosw.parcial.controller.dtos.request.CreateOrderRequest;
import edu.dosw.parcial.controller.dtos.request.UpdateOrderStatusRequest;
import edu.dosw.parcial.controller.dtos.response.OrderResponse;
import edu.dosw.parcial.controller.mappers.OrderMapper;
import edu.dosw.parcial.core.models.OrderStatus;
import edu.dosw.parcial.core.validators.OrderStateValidator;
import edu.dosw.parcial.core.validators.StockValidator;
import edu.dosw.parcial.persistence.entities.OrderEntity;
import edu.dosw.parcial.persistence.entities.OrderItemEntity;
import edu.dosw.parcial.persistence.entities.ProductEntity;
import edu.dosw.parcial.persistence.entities.UserEntity;
import edu.dosw.parcial.persistence.repositories.OrderRepository;
import edu.dosw.parcial.persistence.repositories.ProductRepository;
import edu.dosw.parcial.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final List<OrderStatus> ACTIVE_STATUSES =
            List.of(OrderStatus.CREADO, OrderStatus.EN_PREPARACION);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StockValidator stockValidator;
    private final OrderStateValidator orderStateValidator;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String email) {
        log.info("Creando pedido para usuario: {}", email);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        orderRepository.findByUser_IdAndStatusIn(user.getId(), ACTIVE_STATUSES)
                .ifPresent(o -> { throw new IllegalStateException(
                        "Ya tienes un pedido activo en estado: " + o.getStatus()); });

        List<OrderItemEntity> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (var itemRequest : request.getItems()) {
            ProductEntity product = stockValidator.validateAndGet(
                    itemRequest.getQrCode(), itemRequest.getQuantity());

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            items.add(OrderItemEntity.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build());

            total = total.add(subtotal);
        }

        OrderEntity order = OrderEntity.builder()
                .user(user)
                .status(OrderStatus.CREADO)
                .total(total)
                .build();

        items.forEach(item -> item.setOrder(order));
        order.getItems().addAll(items);

        // Descontar stock de cada producto
        for (int i = 0; i < request.getItems().size(); i++) {
            ProductEntity product = productRepository.findByQrCode(
                    request.getItems().get(i).getQrCode()).orElseThrow();
            product.setStock(product.getStock() - request.getItems().get(i).getQuantity());
            productRepository.save(product);
        }

        OrderEntity saved = orderRepository.save(order);
        log.info("Pedido creado con id: {}", saved.getId());

        return orderMapper.toResponse(saved);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, String email) {
        log.info("Cancelando pedido {} por usuario: {}", orderId, email);

        OrderEntity order = findOrderById(orderId);
        orderStateValidator.validateCancellation(order, email);

        order.setStatus(OrderStatus.CANCELADO);

        OrderEntity saved = orderRepository.save(order);
        log.info("Pedido {} cancelado", orderId);

        return orderMapper.toResponse(saved);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        log.info("Actualizando estado del pedido {} a: {}", orderId, request.getStatus());

        OrderEntity order = findOrderById(orderId);
        OrderStatus newStatus = OrderStatus.valueOf(request.getStatus());

        orderStateValidator.validateAdminTransition(order, newStatus);
        order.setStatus(newStatus);

        OrderEntity saved = orderRepository.save(order);
        log.info("Estado del pedido {} actualizado a {}", orderId, newStatus);

        return orderMapper.toResponse(saved);
    }

    private OrderEntity findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con id: " + orderId));
    }
}
