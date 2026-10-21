package com.prueba.transport_orders.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateOrderRequest {


      @NotBlank(message = "El origen es obligatorio")
    @Size(max = 150, message = "El origen no puede exceder 150 caracteres")
    private String origin;

    @NotBlank(message = "El destino es obligatorio")
    @Size(max = 150, message = "El destino no puede exceder 150 caracteres")
    private String destination;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
