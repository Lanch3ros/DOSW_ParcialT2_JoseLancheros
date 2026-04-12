package edu.dosw.parcial.core.validators;

import edu.dosw.parcial.core.models.OrderStatus;
import edu.dosw.parcial.persistence.entities.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class OrderStateValidator {

    // Transiciones válidas para el admin (señora de la cafetería)
    private static final Map<OrderStatus, Set<OrderStatus>> ADMIN_TRANSITIONS = Map.of(
            OrderStatus.CREADO,         Set.of(OrderStatus.EN_PREPARACION),
            OrderStatus.EN_PREPARACION, Set.of(OrderStatus.ENTREGADO)
    );

    /**
     * Valida que el admin pueda transicionar el pedido al nuevo estado.
     */
    public void validateAdminTransition(OrderEntity order, OrderStatus newStatus) {
        OrderStatus current = order.getStatus();
        log.debug("Validando transición de admin: {} -> {}", current, newStatus);

        Set<OrderStatus> allowed = ADMIN_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException(
                    "Transición inválida: no se puede pasar de " + current + " a " + newStatus);
        }
    }

    /**
     * Valida que el cliente pueda cancelar su propio pedido.
     * Solo se puede cancelar si el pedido está en estado CREADO.
     */
    public void validateCancellation(OrderEntity order, String requestingEmail) {
        log.debug("Validando cancelación del pedido {} por usuario {}", order.getId(), requestingEmail);

        if (!order.getUser().getEmail().equals(requestingEmail)) {
            throw new IllegalArgumentException("No tienes permiso para cancelar este pedido");
        }

        if (order.getStatus() != OrderStatus.CREADO) {
            throw new IllegalStateException(
                    "Solo se puede cancelar un pedido en estado CREADO. Estado actual: " + order.getStatus());
        }
    }
}
