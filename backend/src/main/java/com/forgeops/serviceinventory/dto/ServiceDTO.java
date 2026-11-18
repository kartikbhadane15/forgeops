package com.forgeops.serviceinventory.dto;

import com.forgeops.serviceinventory.Criticality;
import com.forgeops.serviceinventory.LifecycleStatus;
import com.forgeops.serviceinventory.ServiceEntity;

import java.time.Instant;
import java.util.UUID;

public class ServiceDTO {
    private UUID id;
    private String serviceKey;
    private String name;
    private String description;
    private String owningTeam;
    private Criticality criticality;
    private LifecycleStatus lifecycleStatus;
    private Instant createdAt;
    private Instant updatedAt;

    public ServiceDTO() {}

    public static ServiceDTO fromEntity(ServiceEntity e) {
        ServiceDTO d = new ServiceDTO();
        d.id = e.getId();
        d.serviceKey = e.getServiceKey();
        d.name = e.getName();
        d.description = e.getDescription();
        d.owningTeam = e.getOwningTeam();
        d.criticality = e.getCriticality();
        d.lifecycleStatus = e.getLifecycleStatus();
        d.createdAt = e.getCreatedAt();
        d.updatedAt = e.getUpdatedAt();
        return d;
    }

    public UUID getId() { return id; }
    public String getServiceKey() { return serviceKey; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getOwningTeam() { return owningTeam; }
    public Criticality getCriticality() { return criticality; }
    public LifecycleStatus getLifecycleStatus() { return lifecycleStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
