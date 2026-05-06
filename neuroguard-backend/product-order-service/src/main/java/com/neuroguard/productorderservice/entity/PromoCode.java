package com.neuroguard.productorderservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "promo_code")
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    // % discount applied on ORDER TOTAL
    private Double discountPercent;

    // validity window
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;


    // condition: free delivery threshold (dynamic 🔥)
    private Double freeDeliveryThreshold;

    private boolean active;
}