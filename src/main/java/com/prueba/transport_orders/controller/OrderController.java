package com.prueba.transport_orders.controller;

import com.prueba.transport_orders.dto.order.CreateOrderRequest;
import com.prueba.transport_orders.dto.order.OrderFilter;
import com.prueba.transport_orders.dto.order.OrderResponse;
import com.prueba.transport_orders.dto.order.UpdateOrderStatusRequest;
import com.prueba.transport_orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Órdenes", description = "Gestión de órdenes de transporte")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Crear orden", description = "Crea una orden nueva en estado CREATED")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orden creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orderService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar orden por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden encontrada"),
            @ApiResponse(responseCode = "400", description = "ID con formato inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<OrderResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado de la orden",
            description = "Flujo válido: CREATED -> IN_TRANSIT | CANCELLED; IN_TRANSIT -> DELIVERED | CANCELLED. "
                    + "DELIVERED y CANCELLED son estados finales.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Estado inexistente o faltante"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
            @ApiResponse(responseCode = "422", description = "Transición de estado no permitida")
    })
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable UUID id,
                                                      @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request));
    }

    @GetMapping
    @Operation(summary = "Listar órdenes con filtros",
            description = "Todos los filtros son opcionales y combinables. Origen y destino buscan coincidencia "
                    + "parcial sin distinguir mayúsculas. Las fechas (yyyy-MM-dd) filtran por fecha de creación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de órdenes"),
            @ApiResponse(responseCode = "400", description = "Parámetro con formato inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "422", description = "La fecha 'from' es posterior a 'to'")
    })
    public ResponseEntity<List<OrderResponse>> findAll(@ParameterObject OrderFilter filter) {
        return ResponseEntity.ok(orderService.findAll(filter));
    }
}