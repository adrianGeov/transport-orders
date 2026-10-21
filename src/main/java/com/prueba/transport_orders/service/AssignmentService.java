package com.prueba.transport_orders.service;

import com.prueba.transport_orders.dto.assignment.AssignDriverRequest;
import com.prueba.transport_orders.dto.assignment.AssignmentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AssignmentService {

    AssignmentResponse assignDriver(UUID orderId, AssignDriverRequest request);

    AssignmentResponse attachDocument(UUID orderId, MultipartFile file);

    AssignmentResponse attachImage(UUID orderId, MultipartFile file);

}
