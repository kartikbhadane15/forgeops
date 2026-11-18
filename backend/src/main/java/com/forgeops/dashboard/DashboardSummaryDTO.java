package com.forgeops.dashboard;

public class DashboardSummaryDTO {
    private long totalServices;
    private long healthyServices;
    private long degradedServices;
    private long unavailableServices;
    private long openIncidents;
    private long sev1Count;
    private long sev2Count;
    private long sev3Count;
    private long sev4Count;
    private long pendingApprovalCount;

    public DashboardSummaryDTO() {}

    public long getTotalServices() { return totalServices; }
    public void setTotalServices(long totalServices) { this.totalServices = totalServices; }
    public long getHealthyServices() { return healthyServices; }
    public void setHealthyServices(long healthyServices) { this.healthyServices = healthyServices; }
    public long getDegradedServices() { return degradedServices; }
    public void setDegradedServices(long degradedServices) { this.degradedServices = degradedServices; }
    public long getUnavailableServices() { return unavailableServices; }
    public void setUnavailableServices(long unavailableServices) { this.unavailableServices = unavailableServices; }
    public long getOpenIncidents() { return openIncidents; }
    public void setOpenIncidents(long openIncidents) { this.openIncidents = openIncidents; }
    public long getSev1Count() { return sev1Count; }
    public void setSev1Count(long sev1Count) { this.sev1Count = sev1Count; }
    public long getSev2Count() { return sev2Count; }
    public void setSev2Count(long sev2Count) { this.sev2Count = sev2Count; }
    public long getSev3Count() { return sev3Count; }
    public void setSev3Count(long sev3Count) { this.sev3Count = sev3Count; }
    public long getSev4Count() { return sev4Count; }
    public void setSev4Count(long sev4Count) { this.sev4Count = sev4Count; }
    public long getPendingApprovalCount() { return pendingApprovalCount; }
    public void setPendingApprovalCount(long pendingApprovalCount) { this.pendingApprovalCount = pendingApprovalCount; }
}
