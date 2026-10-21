package com.prueba.transport_orders.controller;

import com.prueba.transport_orders.dto.assignment.AssignDriverRequest;
import com.prueba.transport_orders.dto.assignment.AssignmentResponse;
import com.prueba.transport_orders.service.AssignmentService;
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
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    public ResponseEntity<AssignmentResponse> assignDriver(@PathVariable UUID orderId,
                                                           @Valid @RequestBody AssignDriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assignmentService.assignDriver(orderId, request));
    }

    @PostMapping(value = "/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssignmentResponse> attachDocument(@PathVariable UUID orderId,
                                                             @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(assignmentService.attachDocument(orderId, file));
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssignmentResponse> attachImage(@PathVariable UUID orderId,
                                                          @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(assignmentService.attachImage(orderId, file));
    }

}
