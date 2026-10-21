package com.prueba.transport_orders.repository;

import com.prueba.transport_orders.domain.entity.Assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



import java.util.Optional;
import java.util.UUID;


@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {

     Optional<Assignment> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);

}
