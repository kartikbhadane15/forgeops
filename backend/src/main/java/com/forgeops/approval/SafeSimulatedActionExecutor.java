package com.forgeops.approval;

import com.forgeops.metrics.MetricSampleEntity;
import com.forgeops.metrics.MetricSampleRepository;
import com.forgeops.metrics.MetricType;
import com.forgeops.serviceinventory.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class SafeSimulatedActionExecutor {

    private final ServiceRepository serviceRepository;
    private final EnvironmentRepository environmentRepository;
    private final MetricSampleRepository metricSampleRepository;

    public SafeSimulatedActionExecutor(
            ServiceRepository serviceRepository,
            EnvironmentRepository environmentRepository,
            MetricSampleRepository metricSampleRepository
    ) {
        this.serviceRepository = serviceRepository;
        this.environmentRepository = environmentRepository;
        this.metricSampleRepository = metricSampleRepository;
    }

    public String execute(ActionType actionType, UUID serviceId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceId));

        List<EnvironmentEntity> envs = environmentRepository.findByServiceId(serviceId);

        return switch (actionType) {
            case MARK_SERVICE_MAINTENANCE -> {
                service.setLifecycleStatus(LifecycleStatus.MAINTENANCE);
                serviceRepository.save(service);
                for (EnvironmentEntity env : envs) {
                    env.setStatus(EnvironmentStatus.DEGRADED);
                    environmentRepository.save(env);
                }
                yield String.format("SUCCESS: Service '%s' lifecycle transitioned to MAINTENANCE. Active traffic isolated across %d environment(s).",
                        service.getName(), envs.size());
            }

            case CLEAR_DEMO_ALERT -> {
                // Record synthetic healthy baseline error rate sample
                for (EnvironmentEntity env : envs) {
                    MetricSampleEntity resetSample = new MetricSampleEntity(
                            service, env, MetricType.ERROR_RATE, 0.05, "%"
                    );
                    metricSampleRepository.save(resetSample);
                }
                yield String.format("SUCCESS: Alert triggers cleared for '%s'. Simulated error rate counter reset to 0.05%% baseline across %d environment(s).",
                        service.getName(), envs.size());
            }

            case RESTART_DEMO_SERVICE -> {
                service.setLifecycleStatus(LifecycleStatus.ACTIVE);
                serviceRepository.save(service);
                for (EnvironmentEntity env : envs) {
                    env.setStatus(EnvironmentStatus.HEALTHY);
                    environmentRepository.save(env);

                    // Insert fresh nominal metrics representing healthy state
                    metricSampleRepository.save(new MetricSampleEntity(service, env, MetricType.LATENCY_P95, 115.0, "ms"));
                    metricSampleRepository.save(new MetricSampleEntity(service, env, MetricType.ERROR_RATE, 0.02, "%"));
                    metricSampleRepository.save(new MetricSampleEntity(service, env, MetricType.CPU_UTILIZATION, 28.0, "%"));
                    metricSampleRepository.save(new MetricSampleEntity(service, env, MetricType.MEMORY_UTILIZATION, 42.0, "%"));
                }
                yield String.format("SUCCESS: Simulated rolling restart completed for '%s'. Process memory freed, connection pools re-initialized, and status restored to HEALTHY.",
                        service.getName());
            }
        };
    }
}
