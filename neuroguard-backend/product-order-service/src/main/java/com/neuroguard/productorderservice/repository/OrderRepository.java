package com.neuroguard.productorderservice.repository;

import com.neuroguard.productorderservice.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.lines l LEFT JOIN FETCH l.product WHERE o.id = :id")
    Optional<Order> findByIdWithLinesAndProducts(@Param("id") Long id);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.lines l LEFT JOIN FETCH l.product")
    List<Order> findAllWithLinesAndProducts();

    Page<Order> findByStatusContainingIgnoreCase(String status, Pageable pageable);
}
