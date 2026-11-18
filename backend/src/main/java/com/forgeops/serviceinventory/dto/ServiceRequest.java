package com.forgeops.serviceinventory.dto;

import com.forgeops.serviceinventory.Criticality;
import com.forgeops.serviceinventory.LifecycleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ServiceRequest {

    @NotBlank(message = "Service key is required.")
    @Size(max = 64, message = "Service key must be 64 characters or fewer.")
    private String serviceKey;

    @NotBlank(message = "Name is required.")
    @Size(max = 128)
    private String name;

    private String description;

    @NotBlank(message = "Owning team is required.")
    private String owningTeam;

    @NotNull(message = "Criticality is required.")
    private Criticality criticality;

    private LifecycleStatus lifecycleStatus = LifecycleStatus.ACTIVE;

    public ServiceRequest() {}

    public String getServiceKey() { return serviceKey; }
    public void setServiceKey(String serviceKey) { this.serviceKey = serviceKey; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOwningTeam() { return owningTeam; }
    public void setOwningTeam(String owningTeam) { this.owningTeam = owningTeam; }
    public Criticality getCriticality() { return criticality; }
    public void setCriticality(Criticality criticality) { this.criticality = criticality; }
    public LifecycleStatus getLifecycleStatus() { return lifecycleStatus; }
    public void setLifecycleStatus(LifecycleStatus lifecycleStatus) { this.lifecycleStatus = lifecycleStatus; }
}
