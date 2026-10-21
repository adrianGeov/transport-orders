package com.prueba.transport_orders.service.impl;

import com.prueba.transport_orders.domain.entity.Assignment;
import com.prueba.transport_orders.domain.entity.Driver;
import com.prueba.transport_orders.domain.entity.Order;
import com.prueba.transport_orders.domain.enums.OrderStatus;
import com.prueba.transport_orders.dto.assignment.AssignDriverRequest;
import com.prueba.transport_orders.dto.assignment.AssignmentResponse;
import com.prueba.transport_orders.exception.BusinessRuleException;
import com.prueba.transport_orders.exception.ResourceNotFoundException;
import com.prueba.transport_orders.mapper.AssignmentMapper;
import com.prueba.transport_orders.repository.AssignmentRepository;
import com.prueba.transport_orders.repository.DriverRepository;
import com.prueba.transport_orders.repository.OrderRepository;
import com.prueba.transport_orders.service.AssignmentService;
import com.prueba.transport_orders.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private static final Logger log = LoggerFactory.getLogger(AssignmentServiceImpl.class);

    private final AssignmentRepository assignmentRepository;
    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final FileStorageService fileStorageService;
    private final AssignmentMapper assignmentMapper;

    public AssignmentServiceImpl(AssignmentRepository assignmentRepository,
            OrderRepository orderRepository,
            DriverRepository driverRepository,
            FileStorageService fileStorageService,
            AssignmentMapper assignmentMapper) {
        this.assignmentRepository = assignmentRepository;
        this.orderRepository = orderRepository;
        this.driverRepository = driverRepository;
        this.fileStorageService = fileStorageService;
        this.assignmentMapper = assignmentMapper;
    }

    @Override
    @Transactional
    public AssignmentResponse assignDriver(UUID orderId, AssignDriverRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", orderId));
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Conductor", request.getDriverId()));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessRuleException(
                    "Solo se pueden asignar órdenes en estado CREATED. Estado actual: " + order.getStatus());
        }
        if (!driver.isActive()) {
            throw new BusinessRuleException("El conductor " + driver.getId() + " no está activo");
        }
        if (assignmentRepository.existsByOrderId(orderId)) {
            throw new BusinessRuleException("La orden " + orderId + " ya tiene un conductor asignado");
        }

        Assignment assignment = new Assignment();
        assignment.setOrder(order);
        assignment.setDriver(driver);

        Assignment saved = assignmentRepository.save(assignment);
        log.info("Conductor id={} asignado a orden id={}", driver.getId(), orderId);
        return assignmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AssignmentResponse attachDocument(UUID orderId, MultipartFile file) {
        Assignment assignment = getAssignmentOrThrow(orderId);
        String path = fileStorageService.storeDocument(assignment.getId(), file);
        assignment.setDocumentPath(path);
        log.info("PDF agregado a la asignación de la orden id={}", orderId);
        return assignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public AssignmentResponse attachImage(UUID orderId, MultipartFile file) {
        Assignment assignment = getAssignmentOrThrow(orderId);
        String path = fileStorageService.storeImage(assignment.getId(), file);
        assignment.setImagePath(path);
        log.info("Imagen agregada a la asignación de la orden id={}", orderId);
        return assignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    private Assignment getAssignmentOrThrow(UUID orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Orden", orderId);
        }
        return assignmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessRuleException(
                        "La orden " + orderId + " no tiene un conductor asignado"));
    }

}
