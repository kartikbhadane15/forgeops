package com.forgeops.dashboard;

import com.forgeops.serviceinventory.EnvironmentStatus;

import java.util.UUID;

public class ServiceHealthDTO {
    private UUID serviceId;
    private String serviceKey;
    private String name;
    private String owningTeam;
    private String criticality;
    private String lifecycleStatus;
    private EnvironmentStatus productionHealth;
    private double latencyP95Ms;
    private double errorRatePct;
    private int openIncidentCount;

    public ServiceHealthDTO() {}

    public UUID getServiceId() { return serviceId; }
    public void setServiceId(UUID serviceId) { this.serviceId = serviceId; }
    public String getServiceKey() { return serviceKey; }
    public void setServiceKey(String serviceKey) { this.serviceKey = serviceKey; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOwningTeam() { return owningTeam; }
    public void setOwningTeam(String owningTeam) { this.owningTeam = owningTeam; }
    public String getCriticality() { return criticality; }
    public void setCriticality(String criticality) { this.criticality = criticality; }
    public String getLifecycleStatus() { return lifecycleStatus; }
    public void setLifecycleStatus(String lifecycleStatus) { this.lifecycleStatus = lifecycleStatus; }
    public EnvironmentStatus getProductionHealth() { return productionHealth; }
    public void setProductionHealth(EnvironmentStatus productionHealth) { this.productionHealth = productionHealth; }
    public double getLatencyP95Ms() { return latencyP95Ms; }
    public void setLatencyP95Ms(double latencyP95Ms) { this.latencyP95Ms = latencyP95Ms; }
    public double getErrorRatePct() { return errorRatePct; }
    public void setErrorRatePct(double errorRatePct) { this.errorRatePct = errorRatePct; }
    public int getOpenIncidentCount() { return openIncidentCount; }
    public void setOpenIncidentCount(int openIncidentCount) { this.openIncidentCount = openIncidentCount; }
}
