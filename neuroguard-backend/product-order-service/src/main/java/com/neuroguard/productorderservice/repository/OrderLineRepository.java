package com.neuroguard.productorderservice.repository;

import com.neuroguard.productorderservice.entity.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

    boolean existsByProduct_Id(Long productId);
}
