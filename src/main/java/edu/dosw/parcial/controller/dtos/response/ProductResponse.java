package edu.dosw.parcial.controller.dtos.response;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String qrCode;
    private Integer stock;
    private String status;
}