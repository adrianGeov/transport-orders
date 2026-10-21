package com.prueba.transport_orders.mapper;


import org.springframework.stereotype.Component;

import com.prueba.transport_orders.domain.entity.Driver;
import com.prueba.transport_orders.dto.driver.CreateDriverRequest;
import com.prueba.transport_orders.dto.driver.DriverResponse;

@Component
public class DriverMapper {

    public Driver toEntity(CreateDriverRequest request) {
        Driver driver = new Driver();
        driver.setName(request.getName().trim());
        driver.setLicenseNumber(request.getLicenseNumber().trim().toUpperCase());
        driver.setActive(true);
        return driver;
    }

    public DriverResponse toResponse(Driver driver) {
        DriverResponse response = new DriverResponse();
        response.setId(driver.getId());
        response.setName(driver.getName());
        response.setLicenseNumber(driver.getLicenseNumber());
        response.setActive(driver.isActive());
        return response;
    }

}
