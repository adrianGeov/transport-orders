package com.prueba.transport_orders.dto.assignment;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AssignDriverRequest {

    @NotNull(message = "El id del conductor es obligatorio")
    private UUID driverId;

    public UUID getDriverId() {
        return driverId;
    }

    public void setDriverId(UUID driverId) {
        this.driverId = driverId;
    }

}
