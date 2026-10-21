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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

     @Mock
    private OrderRepository orderRepository;

    @Spy
    private OrderMapper orderMapper = new OrderMapper();

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Crea una orden en estado CREATED")
    void create_validRequest_returnsCreatedOrder() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrigin("Ciudad de México");
        request.setDestination("Puebla");

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            return order;
        });

        OrderResponse response = orderService.create(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(response.getOrigin()).isEqualTo("Ciudad de México");
        assertThat(response.getDestination()).isEqualTo("Puebla");
    }

    @Test
    @DisplayName("Devuelve la orden cuando existe")
    void findById_existingOrder_returnsOrder() {
        Order order = buildOrder(OrderStatus.CREATED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        OrderResponse response = orderService.findById(order.getId());

        assertThat(response.getId()).isEqualTo(order.getId());
    }

    @Test
    @DisplayName("Lanza excepción cuando la orden no existe")
    void findById_missingOrder_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Cambia el estado cuando la transición es válida")
    void updateStatus_validTransition_updatesOrder() {
        Order order = buildOrder(OrderStatus.CREATED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.saveAndFlush(order)).thenReturn(order);

        OrderResponse response = orderService.updateStatus(order.getId(), statusRequest(OrderStatus.IN_TRANSIT));

        assertThat(response.getStatus()).isEqualTo(OrderStatus.IN_TRANSIT);
        verify(orderRepository).saveAndFlush(order);
    }

    @Test
    @DisplayName("Rechaza una transición inválida y no guarda cambios")
    void updateStatus_invalidTransition_throwsException() {
        Order order = buildOrder(OrderStatus.DELIVERED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(order.getId(), statusRequest(OrderStatus.CREATED)))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("DELIVERED -> CREATED");

        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Lanza excepción al cambiar estado de una orden inexistente")
    void updateStatus_missingOrder_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(id, statusRequest(OrderStatus.IN_TRANSIT)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("Lista las órdenes aplicando los filtros")
    void findAll_validFilter_returnsOrders() {
        Order order = buildOrder(OrderStatus.CREATED);
        when(orderRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(order));

        OrderFilter filter = new OrderFilter();
        filter.setStatus(OrderStatus.CREATED);

        List<OrderResponse> result = orderService.findAll(filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("Rechaza un rango de fechas invertido sin consultar la BD")
    void findAll_fromAfterTo_throwsException() {
        OrderFilter filter = new OrderFilter();
        filter.setFrom(LocalDate.of(2026, 10, 31));
        filter.setTo(LocalDate.of(2026, 10, 1));

        assertThatThrownBy(() -> orderService.findAll(filter))
                .isInstanceOf(BusinessRuleException.class);

        verifyNoInteractions(orderRepository);
    }

    private Order buildOrder(OrderStatus status) {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus(status);
        order.setOrigin("Ciudad de México");
        order.setDestination("Puebla");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    private UpdateOrderStatusRequest statusRequest(OrderStatus status) {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(status);
        return request;
    }

}
