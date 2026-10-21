package com.prueba.transport_orders.mapper;


import org.springframework.stereotype.Component;

import com.prueba.transport_orders.domain.entity.Assignment;
import com.prueba.transport_orders.dto.assignment.AssignmentResponse;

@Component
public class AssignmentMapper {

    public AssignmentResponse toResponse(Assignment assignment) {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(assignment.getId());
        response.setOrderId(assignment.getOrder().getId());
        response.setDriverId(assignment.getDriver().getId());
        response.setDriverName(assignment.getDriver().getName());
        response.setAssignedAt(assignment.getAssignedAt());
        response.setHasDocument(assignment.getDocumentPath() != null);
        response.setHasImage(assignment.getImagePath() != null);
        return response;
    }

}
