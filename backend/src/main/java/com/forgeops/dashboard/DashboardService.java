package com.forgeops.dashboard;

import com.forgeops.metrics.MetricSampleRepository;
import com.forgeops.metrics.MetricType;
import com.forgeops.serviceinventory.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DashboardService {

    private final ServiceRepository serviceRepo;
    private final EnvironmentRepository envRepo;
    private final MetricSampleRepository metricRepo;

    public DashboardService(ServiceRepository serviceRepo, EnvironmentRepository envRepo,
                            MetricSampleRepository metricRepo) {
        this.serviceRepo = serviceRepo;
        this.envRepo = envRepo;
        this.metricRepo = metricRepo;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary() {
        DashboardSummaryDTO dto = new DashboardSummaryDTO();

        List<ServiceEntity> allServices = serviceRepo.findAll();
        dto.setTotalServices(allServices.size());

        long healthy = 0, degraded = 0, unavailable = 0;
        for (ServiceEntity svc : allServices) {
            EnvironmentStatus health = computeServiceHealth(svc.getId());
            if (health == EnvironmentStatus.HEALTHY) healthy++;
            else if (health == EnvironmentStatus.DEGRADED) degraded++;
            else if (health == EnvironmentStatus.UNAVAILABLE) unavailable++;
        }
        dto.setHealthyServices(healthy);
        dto.setDegradedServices(degraded);
        dto.setUnavailableServices(unavailable);

        // Incidents and approvals will be populated once those modules are built
        dto.setOpenIncidents(0);
        dto.setPendingApprovalCount(0);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<ServiceHealthDTO> getServiceHealthList() {
        return serviceRepo.findAll().stream().map(svc -> {
            ServiceHealthDTO h = new ServiceHealthDTO();
            h.setServiceId(svc.getId());
            h.setServiceKey(svc.getServiceKey());
            h.setName(svc.getName());
            h.setOwningTeam(svc.getOwningTeam());
            h.setCriticality(svc.getCriticality().name());
            h.setLifecycleStatus(svc.getLifecycleStatus().name());
            h.setProductionHealth(computeServiceHealth(svc.getId()));
            h.setLatencyP95Ms(latestMetricValue(svc.getId(), MetricType.LATENCY_P95, 120.0));
            h.setErrorRatePct(latestMetricValue(svc.getId(), MetricType.ERROR_RATE, 0.0));
            h.setOpenIncidentCount(0);
            return h;
        }).toList();
    }

    private EnvironmentStatus computeServiceHealth(java.util.UUID serviceId) {
        List<EnvironmentEntity> envs = envRepo.findByServiceId(serviceId);
        if (envs.stream().anyMatch(e -> e.getStatus() == EnvironmentStatus.UNAVAILABLE)) {
            return EnvironmentStatus.UNAVAILABLE;
        }
        if (envs.stream().anyMatch(e -> e.getStatus() == EnvironmentStatus.DEGRADED)) {
            return EnvironmentStatus.DEGRADED;
        }
        if (envs.isEmpty()) return EnvironmentStatus.UNKNOWN;
        return EnvironmentStatus.HEALTHY;
    }

    private double latestMetricValue(java.util.UUID serviceId, MetricType type, double fallback) {
        Instant since = Instant.now().minus(1, ChronoUnit.HOURS);
        var samples = metricRepo.findByServiceIdAndMetricTypeAndSampledAtAfterOrderBySampledAtDesc(serviceId, type, since);
        if (samples.isEmpty()) return fallback;
        return samples.get(0).getMetricValue();
    }
}
