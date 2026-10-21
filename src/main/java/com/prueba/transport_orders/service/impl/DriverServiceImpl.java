package com.prueba.transport_orders.service.impl;

import com.prueba.transport_orders.domain.entity.Driver;
import com.prueba.transport_orders.dto.driver.CreateDriverRequest;
import com.prueba.transport_orders.dto.driver.DriverResponse;
import com.prueba.transport_orders.exception.DuplicateResourceException;
import com.prueba.transport_orders.mapper.DriverMapper;
import com.prueba.transport_orders.repository.DriverRepository;
import com.prueba.transport_orders.service.DriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class DriverServiceImpl implements DriverService {

     private static final Logger log = LoggerFactory.getLogger(DriverServiceImpl.class);

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

    public DriverServiceImpl(DriverRepository driverRepository, DriverMapper driverMapper) {
        this.driverRepository = driverRepository;
        this.driverMapper = driverMapper;
    }

    @Override
    @Transactional
    public DriverResponse create(CreateDriverRequest request) {
        Driver driver = driverMapper.toEntity(request);

        if (driverRepository.existsByLicenseNumber(driver.getLicenseNumber())) {
            log.warn("Intento de registrar licencia duplicada: {}", driver.getLicenseNumber());
            throw new DuplicateResourceException(
                    "Ya existe un conductor con la licencia " + driver.getLicenseNumber());
        }

        Driver saved = driverRepository.save(driver);
        log.info("Conductor creado id={}", saved.getId());
        return driverMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> findActive() {
        List<DriverResponse> drivers = driverRepository.findByActiveTrue().stream()
                .map(driverMapper::toResponse)
                .toList();
        log.debug("Conductores activos encontrados: {}", drivers.size());
        return drivers;
    }

}
