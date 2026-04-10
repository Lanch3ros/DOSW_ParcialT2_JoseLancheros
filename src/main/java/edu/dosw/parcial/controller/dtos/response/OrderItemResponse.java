package edu.dosw.parcial.controller.dtos.response;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder
public class OrderItemResponse {
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}