package com.prueba.transport_orders.controller;

import com.prueba.transport_orders.domain.enums.OrderStatus;
import com.prueba.transport_orders.dto.order.OrderResponse;
import com.prueba.transport_orders.exception.InvalidStatusTransitionException;
import com.prueba.transport_orders.exception.ResourceNotFoundException;
import com.prueba.transport_orders.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /orders con datos válidos responde 201 con Location")
    void create_validRequest_returns201() throws Exception {
        OrderResponse response = buildResponse(OrderStatus.CREATED);
        when(orderService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "origin": "Ciudad de México", "destination": "Puebla" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(response.getId().toString()))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @DisplayName("POST /orders con campos vacíos responde 400 con el detalle por campo")
    void create_invalidRequest_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "origin": "", "destination": "" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.length()").value(2));

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("GET /orders/{id} inexistente responde 404")
    void findById_missingOrder_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.findById(id)).thenThrow(new ResourceNotFoundException("Orden", id));

        mockMvc.perform(get("/api/v1/orders/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Orden con id " + id + " no encontrado"));
    }

    @Test
    @DisplayName("GET /orders/{id} con id mal formado responde 400")
    void findById_invalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/orders/{id}", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /orders/{id}/status con transición inválida responde 422")
    void updateStatus_invalidTransition_returns422() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.updateStatus(eq(id), any()))
                .thenThrow(new InvalidStatusTransitionException(OrderStatus.DELIVERED, OrderStatus.CREATED));

        mockMvc.perform(patch("/api/v1/orders/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "CREATED" }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Transición de estado no permitida: DELIVERED -> CREATED"));
    }

    private OrderResponse buildResponse(OrderStatus status) {
        OrderResponse response = new OrderResponse();
        response.setId(UUID.randomUUID());
        response.setStatus(status);
        response.setOrigin("Ciudad de México");
        response.setDestination("Puebla");
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }

   

}
