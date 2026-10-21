package com.prueba.transport_orders.service;

import java.util.List;

import com.prueba.transport_orders.dto.driver.CreateDriverRequest;
import com.prueba.transport_orders.dto.driver.DriverResponse;

public interface DriverService {

    DriverResponse create(CreateDriverRequest request);

    List<DriverResponse> findActive();

}
