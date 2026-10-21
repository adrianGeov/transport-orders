package com.prueba.transport_orders.mapper;


import org.springframework.stereotype.Component;

import com.prueba.transport_orders.domain.entity.Order;
import com.prueba.transport_orders.dto.order.CreateOrderRequest;
import com.prueba.transport_orders.dto.order.OrderResponse;

@Component
public class OrderMapper {


    public Order toEntity(CreateOrderRequest request) {
        Order order = new Order();
        order.setOrigin(request.getOrigin().trim());
        order.setDestination(request.getDestination().trim());
        return order;
    }

    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setStatus(order.getStatus());
        response.setOrigin(order.getOrigin());
        response.setDestination(order.getDestination());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        return response;
    }

}
