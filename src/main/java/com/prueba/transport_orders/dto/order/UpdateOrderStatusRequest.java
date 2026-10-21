package com.prueba.transport_orders.dto.order;


import com.prueba.transport_orders.domain.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateOrderStatusRequest {

     @NotNull(message = "El estado es obligatorio")
    private OrderStatus status;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

}
