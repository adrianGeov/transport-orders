package com.prueba.transport_orders.repository;

import com.prueba.transport_orders.domain.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



import java.util.List;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {

    List<Driver> findByActiveTrue();

    boolean existsByLicenseNumber(String licenseNumber);

}
