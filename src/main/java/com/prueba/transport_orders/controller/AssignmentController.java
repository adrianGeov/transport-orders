package com.prueba.transport_orders.controller;

import com.prueba.transport_orders.dto.assignment.AssignDriverRequest;
import com.prueba.transport_orders.dto.assignment.AssignmentResponse;
import com.prueba.transport_orders.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/assignment")
@Tag(name = "Asignaciones", description = "Asignación de conductores a órdenes y carga de archivos")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    @Operation(summary = "Asignar conductor a una orden",
            description = "Solo si el conductor está activo y la orden está en estado CREATED")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conductor asignado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "404", description = "Orden o conductor no encontrado"),
            @ApiResponse(responseCode = "422", description = "Conductor inactivo, orden no está en CREATED o ya asignada")
    })
    public ResponseEntity<AssignmentResponse> assignDriver(@PathVariable UUID orderId,
                                                           @Valid @RequestBody AssignDriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assignmentService.assignDriver(orderId, request));
    }

    @PostMapping(value = "/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Agregar PDF a la asignación", description = "Solo archivos .pdf, máximo 5MB")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF agregado"),
            @ApiResponse(responseCode = "400", description = "Archivo vacío, faltante o que no es PDF"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
            @ApiResponse(responseCode = "413", description = "Archivo mayor a 5MB"),
            @ApiResponse(responseCode = "422", description = "La orden no tiene conductor asignado")
    })
    public ResponseEntity<AssignmentResponse> attachDocument(@PathVariable UUID orderId,
                                                             @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(assignmentService.attachDocument(orderId, file));
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Agregar imagen a la asignación", description = "Solo archivos .png o .jpg, máximo 5MB")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagen agregada"),
            @ApiResponse(responseCode = "400", description = "Archivo vacío, faltante o que no es PNG/JPG"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
            @ApiResponse(responseCode = "413", description = "Archivo mayor a 5MB"),
            @ApiResponse(responseCode = "422", description = "La orden no tiene conductor asignado")
    })
    public ResponseEntity<AssignmentResponse> attachImage(@PathVariable UUID orderId,
                                                          @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(assignmentService.attachImage(orderId, file));
    }
}