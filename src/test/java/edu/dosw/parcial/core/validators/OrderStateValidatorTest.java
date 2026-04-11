package edu.dosw.parcial.core.validators;

import edu.dosw.parcial.core.models.OrderStatus;
import edu.dosw.parcial.persistence.entities.OrderEntity;
import edu.dosw.parcial.persistence.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class OrderStateValidatorTest {

    private OrderStateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new OrderStateValidator();
    }

    // ── validateAdminTransition ──────────────────────────────────────────────

    @Test
    void adminTransition_creadoToEnPreparacion_isValid() {
        OrderEntity order = buildOrder(OrderStatus.CREADO, "user@eci.edu.co");

        assertThatNoException().isThrownBy(() ->
                validator.validateAdminTransition(order, OrderStatus.EN_PREPARACION));
    }

    @Test
    void adminTransition_enPreparacionToEntregado_isValid() {
        OrderEntity order = buildOrder(OrderStatus.EN_PREPARACION, "user@eci.edu.co");

        assertThatNoException().isThrownBy(() ->
                validator.validateAdminTransition(order, OrderStatus.ENTREGADO));
    }

    @Test
    void adminTransition_creadoToEntregado_throwsIllegalState() {
        OrderEntity order = buildOrder(OrderStatus.CREADO, "user@eci.edu.co");

        assertThatThrownBy(() -> validator.validateAdminTransition(order, OrderStatus.ENTREGADO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transición inválida");
    }

    @Test
    void adminTransition_canceladoToAny_throwsIllegalState() {
        OrderEntity order = buildOrder(OrderStatus.CANCELADO, "user@eci.edu.co");

        assertThatThrownBy(() -> validator.validateAdminTransition(order, OrderStatus.EN_PREPARACION))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transición inválida");
    }

    @Test
    void adminTransition_entregadoToAny_throwsIllegalState() {
        OrderEntity order = buildOrder(OrderStatus.ENTREGADO, "user@eci.edu.co");

        assertThatThrownBy(() -> validator.validateAdminTransition(order, OrderStatus.EN_PREPARACION))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── validateCancellation ────────────────────────────────────────────────

    @Test
    void cancellation_ownerAndCreado_isValid() {
        OrderEntity order = buildOrder(OrderStatus.CREADO, "cliente@eci.edu.co");

        assertThatNoException().isThrownBy(() ->
                validator.validateCancellation(order, "cliente@eci.edu.co"));
    }

    @Test
    void cancellation_wrongUser_throwsIllegalArgument() {
        OrderEntity order = buildOrder(OrderStatus.CREADO, "cliente@eci.edu.co");

        assertThatThrownBy(() -> validator.validateCancellation(order, "otro@eci.edu.co"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No tienes permiso");
    }

    @Test
    void cancellation_ownerButEnPreparacion_throwsIllegalState() {
        OrderEntity order = buildOrder(OrderStatus.EN_PREPARACION, "cliente@eci.edu.co");

        assertThatThrownBy(() -> validator.validateCancellation(order, "cliente@eci.edu.co"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("estado CREADO");
    }

    @Test
    void cancellation_ownerButEntregado_throwsIllegalState() {
        OrderEntity order = buildOrder(OrderStatus.ENTREGADO, "cliente@eci.edu.co");

        assertThatThrownBy(() -> validator.validateCancellation(order, "cliente@eci.edu.co"))
                .isInstanceOf(IllegalStateException.class);
    }

    private OrderEntity buildOrder(OrderStatus status, String userEmail) {
        UserEntity user = UserEntity.builder()
                .id(1L)
                .email(userEmail)
                .build();

        return OrderEntity.builder()
                .id(1L)
                .user(user)
                .status(status)
                .build();
    }
}
