package edu.dosw.parcial.controller.dtos.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrderItemRequest {

    @NotBlank(message = "El código QR es obligatorio")
    private String qrCode;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer quantity;
}