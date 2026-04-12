package edu.dosw.parcial.controller.dtos.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(
            regexp = "EN_PREPARACION|ENTREGADO",
            message = "Estado inválido. Use EN_PREPARACION o ENTREGADO"
    )
    private String status;
}