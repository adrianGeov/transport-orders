package com.prueba.transport_orders.service.impl;

import com.prueba.transport_orders.domain.entity.Order;
import com.prueba.transport_orders.domain.enums.OrderStatus;
import com.prueba.transport_orders.dto.order.CreateOrderRequest;
import com.prueba.transport_orders.dto.order.OrderFilter;
import com.prueba.transport_orders.dto.order.OrderResponse;
import com.prueba.transport_orders.dto.order.UpdateOrderStatusRequest;
import com.prueba.transport_orders.exception.BusinessRuleException;
import com.prueba.transport_orders.exception.InvalidStatusTransitionException;
import com.prueba.transport_orders.exception.ResourceNotFoundException;
import com.prueba.transport_orders.mapper.OrderMapper;
import com.prueba.transport_orders.repository.OrderRepository;
import com.prueba.transport_orders.repository.specification.OrderSpecifications;
import com.prueba.transport_orders.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class OrderServiceImpl implements OrderService {

     private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Order order = orderMapper.toEntity(request);
        order.setStatus(OrderStatus.CREATED);

        Order saved = orderRepository.save(order);
        log.info("Orden creada id={} origen='{}' destino='{}'",
                saved.getId(), saved.getOrigin(), saved.getDestination());
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(UUID id) {
        return orderMapper.toResponse(getOrderOrThrow(id));
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID id, UpdateOrderStatusRequest request) {
        Order order = getOrderOrThrow(id);
        OrderStatus current = order.getStatus();
        OrderStatus target = request.getStatus();

        if (!current.canTransitionTo(target)) {
            throw new InvalidStatusTransitionException(current, target);
        }

        order.setStatus(target);
        Order saved = orderRepository.saveAndFlush(order);
        log.info("Orden id={} cambió de estado {} -> {}", id, current, target);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll(OrderFilter filter) {
        if (filter.getFrom() != null && filter.getTo() != null && filter.getFrom().isAfter(filter.getTo())) {
            throw new BusinessRuleException("La fecha 'from' no puede ser posterior a la fecha 'to'");
        }

        List<OrderResponse> orders = orderRepository
                .findAll(OrderSpecifications.withFilter(filter), Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(orderMapper::toResponse)
                .toList();

        log.debug("Consulta de órdenes: {} resultados", orders.size());
        return orders;
    }

    private Order getOrderOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", id));
    }

}
