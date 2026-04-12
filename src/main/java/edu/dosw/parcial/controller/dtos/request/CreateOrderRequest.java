package edu.dosw.parcial.controller.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {

    @NotEmpty(message = "El pedido debe tener al menos un producto")
    @Valid
    private List<OrderItemRequest> items;
}