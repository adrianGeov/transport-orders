package com.prueba.transport_orders.service.impl;

import com.prueba.transport_orders.domain.entity.Driver;
import com.prueba.transport_orders.dto.driver.CreateDriverRequest;
import com.prueba.transport_orders.dto.driver.DriverResponse;
import com.prueba.transport_orders.exception.DuplicateResourceException;
import com.prueba.transport_orders.mapper.DriverMapper;
import com.prueba.transport_orders.repository.DriverRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class DriverServiceImplTest {

    @Mock
    private DriverRepository driverRepository;

    @Spy
    private DriverMapper driverMapper = new DriverMapper();

    @InjectMocks
    private DriverServiceImpl driverService;

    @Test
    @DisplayName("Crea un conductor activo normalizando nombre y licencia")
    void create_validRequest_savesActiveDriver() {
        CreateDriverRequest request = new CreateDriverRequest();
        request.setName("  Juan Pérez  ");
        request.setLicenseNumber("cdmx-12345");

        when(driverRepository.existsByLicenseNumber("CDMX-12345")).thenReturn(false);
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> {
            Driver driver = invocation.getArgument(0);
            driver.setId(UUID.randomUUID());
            return driver;
        });

        DriverResponse response = driverService.create(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("Juan Pérez");
        assertThat(response.getLicenseNumber()).isEqualTo("CDMX-12345");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("Lanza excepción si la licencia ya existe y no guarda nada")
    void create_duplicateLicense_throwsException() {
        CreateDriverRequest request = new CreateDriverRequest();
        request.setName("Juan Pérez");
        request.setLicenseNumber("CDMX-12345");

        when(driverRepository.existsByLicenseNumber("CDMX-12345")).thenReturn(true);

        assertThatThrownBy(() -> driverService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("CDMX-12345");

        verify(driverRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lista únicamente los conductores activos")
    void findActive_returnsMappedDrivers() {
        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());
        driver.setName("Ana López");
        driver.setLicenseNumber("JAL-99999");
        driver.setActive(true);

        when(driverRepository.findByActiveTrue()).thenReturn(List.of(driver));

        List<DriverResponse> result = driverService.findActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Ana López");
        assertThat(result.get(0).isActive()).isTrue();
    }

}
