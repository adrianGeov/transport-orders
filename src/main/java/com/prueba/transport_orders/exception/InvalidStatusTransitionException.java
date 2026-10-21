package com.prueba.transport_orders.exception;

import com.prueba.transport_orders.domain.enums.OrderStatus;

public class InvalidStatusTransitionException extends BusinessRuleException {

    public InvalidStatusTransitionException(OrderStatus current, OrderStatus target) {
        super("Transición de estado no permitida: " + current + " -> " + target);
    }

}
