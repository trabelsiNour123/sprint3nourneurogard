package com.neuroguard.productorderservice.repository;

import com.neuroguard.productorderservice.entity.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    boolean existsByOrder_Id(Long orderId);

    Optional<Delivery> findByOrder_Id(Long orderId);

    /**
     * Case-insensitive match on address, delivery status (enum name), or order id (substring).
     */
    @Query("""
            SELECT d FROM Delivery d JOIN d.order o
            WHERE LOWER(d.address) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(STR(d.status)) LIKE LOWER(CONCAT('%', :q, '%'))
               OR STR(o.id) LIKE CONCAT('%', :q, '%')
            """)
    Page<Delivery> searchDeliveries(@Param("q") String q, Pageable pageable);
}
