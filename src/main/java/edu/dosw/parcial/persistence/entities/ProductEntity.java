package edu.dosw.parcial.persistence.entities;

import edu.dosw.parcial.core.models.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductEntity {

    @Id
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "Texto")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "qr_code", nullable = false)
    private String qrCode;

    @Column(nullable = false)
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;
}