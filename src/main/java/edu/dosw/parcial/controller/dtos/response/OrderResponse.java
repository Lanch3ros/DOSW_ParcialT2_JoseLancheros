package edu.dosw.parcial.controller.dtos.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class OrderResponse {
    private String id;
    private String userId;
    private List<OrderItemResponse> items;
    private String status;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}