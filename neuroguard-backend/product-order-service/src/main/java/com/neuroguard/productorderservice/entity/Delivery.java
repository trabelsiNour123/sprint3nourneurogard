package com.neuroguard.productorderservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Owning side of the one-to-one: FK column {@code id_commande} references {@link Order}.
 */
@Entity
@Table(name = "livraisons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_livraison")
    private Long id;

    @Column(name = "date_livraison", nullable = false)
    private LocalDateTime deliveryDate;

    @Column(name = "adresse", nullable = false, length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_livraison", nullable = false, length = 32)
    private DeliveryStatus status;

    private Double fee;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commande", nullable = false, unique = true, referencedColumnName = "order_id")
    @JsonIgnore
    private Order order;

    @JsonProperty("orderId")
    public Long getOrderId() {
        return order == null ? null : order.getId();
    }
}
