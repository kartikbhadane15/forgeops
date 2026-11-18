package com.forgeops.serviceinventory;

import com.forgeops.common.exception.BusinessRuleException;
import com.forgeops.common.exception.ResourceNotFoundException;
import com.forgeops.serviceinventory.dto.EnvironmentDTO;
import com.forgeops.serviceinventory.dto.ServiceDTO;
import com.forgeops.serviceinventory.dto.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ServiceInventoryService {

    private final ServiceRepository serviceRepo;
    private final EnvironmentRepository environmentRepo;

    public ServiceInventoryService(ServiceRepository serviceRepo, EnvironmentRepository environmentRepo) {
        this.serviceRepo = serviceRepo;
        this.environmentRepo = environmentRepo;
    }

    @Transactional(readOnly = true)
    public Page<ServiceDTO> listServices(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return serviceRepo.findByNameContainingIgnoreCaseOrServiceKeyContainingIgnoreCase(
                    search.trim(), search.trim(), pageable).map(ServiceDTO::fromEntity);
        }
        return serviceRepo.findAll(pageable).map(ServiceDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public ServiceDTO getService(UUID id) {
        return ServiceDTO.fromEntity(findById(id));
    }

    @Transactional
    public ServiceDTO createService(ServiceRequest req) {
        if (serviceRepo.existsByServiceKey(req.getServiceKey())) {
            throw new BusinessRuleException("SERVICE_KEY_EXISTS", "Service key '" + req.getServiceKey() + "' is already registered.");
        }
        ServiceEntity entity = new ServiceEntity();
        mapRequestToEntity(req, entity);
        return ServiceDTO.fromEntity(serviceRepo.save(entity));
    }

    @Transactional
    public ServiceDTO updateService(UUID id, ServiceRequest req) {
        ServiceEntity entity = findById(id);
        if (!entity.getServiceKey().equals(req.getServiceKey()) && serviceRepo.existsByServiceKey(req.getServiceKey())) {
            throw new BusinessRuleException("SERVICE_KEY_EXISTS", "Service key '" + req.getServiceKey() + "' is already registered.");
        }
        mapRequestToEntity(req, entity);
        return ServiceDTO.fromEntity(serviceRepo.save(entity));
    }

    @Transactional(readOnly = true)
    public List<EnvironmentDTO> getEnvironmentsForService(UUID id) {
        findById(id);
        return environmentRepo.findByServiceId(id).stream().map(EnvironmentDTO::fromEntity).toList();
    }

    private ServiceEntity findById(UUID id) {
        return serviceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));
    }

    private void mapRequestToEntity(ServiceRequest req, ServiceEntity e) {
        e.setServiceKey(req.getServiceKey().trim().toLowerCase());
        e.setName(req.getName().trim());
        e.setDescription(req.getDescription());
        e.setOwningTeam(req.getOwningTeam().trim());
        e.setCriticality(req.getCriticality());
        e.setLifecycleStatus(req.getLifecycleStatus() != null ? req.getLifecycleStatus() : LifecycleStatus.ACTIVE);
    }
}
