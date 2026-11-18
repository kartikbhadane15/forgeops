package com.forgeops.serviceinventory.dto;

import com.forgeops.serviceinventory.EnvironmentEntity;
import com.forgeops.serviceinventory.EnvironmentStatus;

import java.util.UUID;

public class EnvironmentDTO {
    private UUID id;
    private UUID serviceId;
    private String name;
    private String envType;
    private String regionLabel;
    private EnvironmentStatus status;

    public EnvironmentDTO() {}

    public static EnvironmentDTO fromEntity(EnvironmentEntity e) {
        EnvironmentDTO d = new EnvironmentDTO();
        d.id = e.getId();
        d.serviceId = e.getService().getId();
        d.name = e.getName();
        d.envType = e.getEnvType();
        d.regionLabel = e.getRegionLabel();
        d.status = e.getStatus();
        return d;
    }

    public UUID getId() { return id; }
    public UUID getServiceId() { return serviceId; }
    public String getName() { return name; }
    public String getEnvType() { return envType; }
    public String getRegionLabel() { return regionLabel; }
    public EnvironmentStatus getStatus() { return status; }
}
