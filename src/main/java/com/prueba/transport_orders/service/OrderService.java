package com.prueba.transport_orders.service;

import com.prueba.transport_orders.dto.order.CreateOrderRequest;
import com.prueba.transport_orders.dto.order.OrderFilter;
import com.prueba.transport_orders.dto.order.OrderResponse;
import com.prueba.transport_orders.dto.order.UpdateOrderStatusRequest;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponse create(CreateOrderRequest request);

    OrderResponse findById(UUID id);

    OrderResponse updateStatus(UUID id, UpdateOrderStatusRequest request);

    List<OrderResponse> findAll(OrderFilter filter);

}
