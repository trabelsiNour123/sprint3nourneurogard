package com.neuroguard.productorderservice.entity;

/**
 * Persisted as string in {@code statut_livraison}. If you previously used French enum names
 * in the database, run an SQL migration to these values or add a custom converter.
 */
public enum DeliveryStatus {
    PENDING,
    SHIPPED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}
