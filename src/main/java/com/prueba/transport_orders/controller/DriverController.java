package com.prueba.transport_orders.controller;

import com.prueba.transport_orders.dto.driver.CreateDriverRequest;
import com.prueba.transport_orders.dto.driver.DriverResponse;
import com.prueba.transport_orders.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
public class DriverController {

     private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    public ResponseEntity<DriverResponse> create(@Valid @RequestBody CreateDriverRequest request) {
        DriverResponse created = driverService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/active")
    public ResponseEntity<List<DriverResponse>> findActive() {
        return ResponseEntity.ok(driverService.findActive());
    }

}
