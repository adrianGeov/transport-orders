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
import com.prueba.transport_orders.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

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
public class AssignmentServiceImplTest {

     @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Spy
    private AssignmentMapper assignmentMapper = new AssignmentMapper();

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    private Order order;
    private Driver driver;
    private AssignDriverRequest request;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatus.CREATED);
        order.setOrigin("Ciudad de México");
        order.setDestination("Puebla");

        driver = new Driver();
        driver.setId(UUID.randomUUID());
        driver.setName("Juan Pérez");
        driver.setLicenseNumber("CDMX-12345");
        driver.setActive(true);

        request = new AssignDriverRequest();
        request.setDriverId(driver.getId());
    }

    // ---------- Asignación ----------

    @Test
    @DisplayName("Asigna un conductor activo a una orden en estado CREATED")
    void assignDriver_validData_createsAssignment() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
        when(assignmentRepository.existsByOrderId(order.getId())).thenReturn(false);
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment assignment = invocation.getArgument(0);
            assignment.setId(UUID.randomUUID());
            return assignment;
        });

        AssignmentResponse response = assignmentService.assignDriver(order.getId(), request);

        assertThat(response.getOrderId()).isEqualTo(order.getId());
        assertThat(response.getDriverId()).isEqualTo(driver.getId());
        assertThat(response.getDriverName()).isEqualTo("Juan Pérez");
        assertThat(response.isHasDocument()).isFalse();
        assertThat(response.isHasImage()).isFalse();
    }

    @Test
    @DisplayName("Falla si la orden no existe")
    void assignDriver_missingOrder_throwsNotFound() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.assignDriver(order.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Falla si el conductor no existe")
    void assignDriver_missingDriver_throwsNotFound() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(driverRepository.findById(driver.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.assignDriver(order.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Falla si la orden no está en estado CREATED")
    void assignDriver_orderNotCreated_throwsBusinessRule() {
        order.setStatus(OrderStatus.IN_TRANSIT);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));

        assertThatThrownBy(() -> assignmentService.assignDriver(order.getId(), request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CREATED");

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Falla si el conductor está inactivo")
    void assignDriver_inactiveDriver_throwsBusinessRule() {
        driver.setActive(false);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));

        assertThatThrownBy(() -> assignmentService.assignDriver(order.getId(), request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no está activo");

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Falla si la orden ya tiene un conductor asignado")
    void assignDriver_alreadyAssigned_throwsBusinessRule() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
        when(assignmentRepository.existsByOrderId(order.getId())).thenReturn(true);

        assertThatThrownBy(() -> assignmentService.assignDriver(order.getId(), request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya tiene un conductor asignado");

        verify(assignmentRepository, never()).save(any());
    }

    // ---------- Archivos ----------

    @Test
    @DisplayName("Agrega el PDF a una asignación existente")
    void attachDocument_existingAssignment_storesPath() {
        Assignment assignment = buildAssignment();
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "contenido".getBytes());

        when(orderRepository.existsById(order.getId())).thenReturn(true);
        when(assignmentRepository.findByOrderId(order.getId())).thenReturn(Optional.of(assignment));
        when(fileStorageService.storeDocument(assignment.getId(), file)).thenReturn("assignments/x/document.pdf");
        when(assignmentRepository.save(assignment)).thenReturn(assignment);

        AssignmentResponse response = assignmentService.attachDocument(order.getId(), file);

        assertThat(response.isHasDocument()).isTrue();
        assertThat(assignment.getDocumentPath()).isEqualTo("assignments/x/document.pdf");
    }

    @Test
    @DisplayName("Agrega la imagen a una asignación existente")
    void attachImage_existingAssignment_storesPath() {
        Assignment assignment = buildAssignment();
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", "contenido".getBytes());

        when(orderRepository.existsById(order.getId())).thenReturn(true);
        when(assignmentRepository.findByOrderId(order.getId())).thenReturn(Optional.of(assignment));
        when(fileStorageService.storeImage(assignment.getId(), file)).thenReturn("assignments/x/image.png");
        when(assignmentRepository.save(assignment)).thenReturn(assignment);

        AssignmentResponse response = assignmentService.attachImage(order.getId(), file);

        assertThat(response.isHasImage()).isTrue();
    }

    @Test
    @DisplayName("No guarda archivos si la orden no tiene asignación")
    void attachDocument_withoutAssignment_throwsBusinessRule() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "contenido".getBytes());

        when(orderRepository.existsById(order.getId())).thenReturn(true);
        when(assignmentRepository.findByOrderId(order.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.attachDocument(order.getId(), file))
                .isInstanceOf(BusinessRuleException.class);

        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("Falla al subir archivo a una orden inexistente")
    void attachImage_missingOrder_throwsNotFound() {
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", "contenido".getBytes());

        when(orderRepository.existsById(order.getId())).thenReturn(false);

        assertThatThrownBy(() -> assignmentService.attachImage(order.getId(), file))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(fileStorageService);
    }

    private Assignment buildAssignment() {
        Assignment assignment = new Assignment();
        assignment.setId(UUID.randomUUID());
        assignment.setOrder(order);
        assignment.setDriver(driver);
        return assignment;
    }

}
