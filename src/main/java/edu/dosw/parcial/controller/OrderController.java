package edu.dosw.parcial.controller;

import edu.dosw.parcial.controller.dtos.request.CreateOrderRequest;
import edu.dosw.parcial.controller.dtos.request.UpdateOrderStatusRequest;
import edu.dosw.parcial.controller.dtos.response.OrderResponse;
import edu.dosw.parcial.core.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pedidos", description = "Creación, cancelación y gestión de estados de pedidos")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Crear pedido", description = "Crea un nuevo pedido con los productos escaneados. Requiere rol CLIENTE")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido creado en estado CREADO"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente"),
        @ApiResponse(responseCode = "409", description = "El usuario ya tiene un pedido activo")
    })
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal String email) {
        log.info("POST /api/orders - usuario: {}", email);
        OrderResponse response = orderService.createOrder(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Cancelar pedido", description = "Cancela el pedido si está en estado CREADO. Requiere rol CLIENTE y ser el dueño del pedido")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido cancelado exitosamente"),
        @ApiResponse(responseCode = "400", description = "El pedido no pertenece al usuario"),
        @ApiResponse(responseCode = "409", description = "El pedido no está en estado CREADO")
    })
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {
        log.info("PATCH /api/orders/{}/cancel - usuario: {}", id, email);
        OrderResponse response = orderService.cancelOrder(id, email);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('CAFETERIA_LADY')")
    @Operation(summary = "Actualizar estado del pedido", description = "Avanza el estado: CREADO→EN_PREPARACION o EN_PREPARACION→ENTREGADO. Requiere rol CAFETERIA_LADY")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Pedido no encontrado"),
        @ApiResponse(responseCode = "409", description = "Transición de estado no permitida")
    })
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        log.info("PATCH /api/orders/{}/status - nuevo estado: {}", id, request.getStatus());
        OrderResponse response = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(response);
    }
}
