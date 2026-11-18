package com.forgeops.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgeops.ai.dto.AnalysisResultDTO;
import com.forgeops.ai.dto.ProposedActionDTO;
import com.forgeops.common.exception.ResourceNotFoundException;
import com.forgeops.incident.IncidentEntity;
import com.forgeops.incident.IncidentRepository;
import com.forgeops.metrics.MetricSampleEntity;
import com.forgeops.metrics.MetricSampleRepository;
import com.forgeops.metrics.MetricType;
import com.forgeops.serviceinventory.EnvironmentEntity;
import com.forgeops.serviceinventory.EnvironmentStatus;
import com.forgeops.serviceinventory.ServiceEntity;
import com.forgeops.user.UserEntity;
import com.forgeops.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RuleBasedIncidentAnalysisService {

    private final IncidentRepository incidentRepository;
    private final IncidentAnalysisRepository analysisRepository;
    private final MetricSampleRepository metricSampleRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public RuleBasedIncidentAnalysisService(
            IncidentRepository incidentRepository,
            IncidentAnalysisRepository analysisRepository,
            MetricSampleRepository metricSampleRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.incidentRepository = incidentRepository;
        this.analysisRepository = analysisRepository;
        this.metricSampleRepository = metricSampleRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<AnalysisResultDTO> getAnalysesForIncident(UUID incidentId) {
        return analysisRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public AnalysisResultDTO runAnalysis(UUID incidentId, UUID requestedById) {
        IncidentEntity incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", incidentId));

        UserEntity user = userRepository.findById(requestedById)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", requestedById));

        ServiceEntity service = incident.getService();
        EnvironmentEntity env = incident.getEnvironment();

        Instant since = Instant.now().minus(2, ChronoUnit.HOURS);
        List<MetricSampleEntity> samples = metricSampleRepository.findByServiceIdAndSampledAtAfterOrderBySampledAtDesc(
                service.getId(), since
        );

        // Compute latest / average values
        double latestLatency = getLatestMetric(samples, MetricType.LATENCY_P95, 120.0);
        double latestErrorRate = getLatestMetric(samples, MetricType.ERROR_RATE, 0.0);
        double latestMemory = getLatestMetric(samples, MetricType.MEMORY_UTILIZATION, 45.0);
        double latestCpu = getLatestMetric(samples, MetricType.CPU_UTILIZATION, 30.0);

        List<String> possibleCauses = new ArrayList<>();
        List<String> investigationSteps = new ArrayList<>();
        List<ProposedActionDTO> proposedActions = new ArrayList<>();
        List<String> limitations = new ArrayList<>();
        String riskLevel = "LOW";
        StringBuilder summary = new StringBuilder();

        boolean ruleTriggered = false;

        // Rule 4: Critical Service Down / Unavailable
        if (env.getStatus() == EnvironmentStatus.UNAVAILABLE) {
            ruleTriggered = true;
            riskLevel = "HIGH";
            summary.append("Environment ").append(env.getName()).append(" is UNAVAILABLE. Total process or health check failure detected. ");
            possibleCauses.add("Uncaught runtime exception crashed primary service process.");
            possibleCauses.add("Underlying host node failure or container eviction.");
            possibleCauses.add("Unhealthy readiness probe failed consecutive thresholds.");

            investigationSteps.add("Inspect container crash logs via cluster orchestration logs.");
            investigationSteps.add("Verify host node memory pressure and kernel OOM events.");
            investigationSteps.add("Check egress network connectivity to database and upstream services.");

            proposedActions.add(new ProposedActionDTO(
                    "RESTART_DEMO_SERVICE",
                    service.getId(),
                    service.getName(),
                    "Perform a simulated graceful restart of the service environment to restore process availability.",
                    "MEDIUM",
                    "If restart fails to restore health, isolate service traffic via maintenance mode."
            ));
            proposedActions.add(new ProposedActionDTO(
                    "MARK_SERVICE_MAINTENANCE",
                    service.getId(),
                    service.getName(),
                    "Route traffic away from failing instance to prevent cascading client timeouts.",
                    "LOW",
                    "Re-enable active traffic once service instances report healthy readiness status."
            ));
        }

        // Rule 1: High Latency & Saturated Downstream
        if (latestLatency > 800.0) {
            ruleTriggered = true;
            if (!"HIGH".equals(riskLevel)) riskLevel = "MEDIUM";
            summary.append(String.format("P95 latency is elevated at %.1fms (threshold > 800ms). ", latestLatency));
            possibleCauses.add("Downstream service latency or network partition causing thread pool backlog.");
            possibleCauses.add("Database connection pool starvation or slow table scans.");
            possibleCauses.add("Lock contention on shared cache or database rows.");

            investigationSteps.add("Review active connection pool metrics and thread dump.");
            investigationSteps.add("Analyze database query latency distribution and slow query logs.");
            investigationSteps.add("Verify network round-trip time to dependent microservices.");

            proposedActions.add(new ProposedActionDTO(
                    "RESTART_DEMO_SERVICE",
                    service.getId(),
                    service.getName(),
                    "Restart instance to flush starved connection pools and reset clogged worker threads.",
                    "MEDIUM",
                    "Can be triggered after verifying downstream dependencies have recovered."
            ));
        }

        // Rule 2: Memory Leak / Saturated Heap
        if (latestMemory > 85.0) {
            ruleTriggered = true;
            if (!"HIGH".equals(riskLevel)) riskLevel = "MEDIUM";
            summary.append(String.format("Memory utilization is saturated at %.1f%% (threshold > 85%%). ", latestMemory));
            possibleCauses.add("JVM Heap exhaustion due to memory leak or unbounded cache allocation.");
            possibleCauses.add("Large payload ingestion causing full GC pause storm.");

            investigationSteps.add("Capture and inspect heap histogram for uncollected collections.");
            investigationSteps.add("Check GC duration and frequency in telemetry logs.");

            proposedActions.add(new ProposedActionDTO(
                    "RESTART_DEMO_SERVICE",
                    service.getId(),
                    service.getName(),
                    "Restart service to free heap space and recover from memory fragmentation.",
                    "MEDIUM",
                    "Capture heap dump prior to restart if persistence permits."
            ));
        }

        // Rule 3: Error Rate Spike
        if (latestErrorRate > 5.0) {
            ruleTriggered = true;
            if (!"HIGH".equals(riskLevel)) riskLevel = "LOW";
            summary.append(String.format("Error rate is anomalous at %.2f%% (threshold > 5.0%%). ", latestErrorRate));
            possibleCauses.add("Recent configuration drift or invalid upstream request payload.");
            possibleCauses.add("Third-party authentication or payment webhook rejecting payloads.");

            investigationSteps.add("Filter HTTP 5xx log traces by upstream endpoint.");
            investigationSteps.add("Compare error spike timestamp against recent deployment and config change logs.");

            proposedActions.add(new ProposedActionDTO(
                    "CLEAR_DEMO_ALERT",
                    service.getId(),
                    service.getName(),
                    "Reset error rate threshold counter once transient upstream condition has resolved.",
                    "LOW",
                    "Alert will immediately re-trigger if persistent 5xx responses continue."
            ));
        }

        if (!ruleTriggered) {
            summary.append("Telemetry metrics currently within nominal thresholds. No acute operational fault pattern matched. ");
            possibleCauses.add("Intermittent client-side connectivity fluctuation.");
            possibleCauses.add("Synthetic alert triggered prior to metric normalization.");
            investigationSteps.add("Monitor metric trends over the next 15-minute polling window.");
            investigationSteps.add("Review application error logs for non-fatal warning spikes.");
            limitations.add("Metric telemetry is sampled periodically; transient micro-bursts (<60s) may not appear in rollups.");
        }

        // Standard safety limitations appended to every analysis
        limitations.add("Analysis is 100% offline and deterministic; external network dependencies and third-party SaaS status pages are not polled.");
        limitations.add("Proposed actions are advisory recommendations only and strictly require approval from an authorized operations reviewer before execution.");

        IncidentAnalysisEntity entity = new IncidentAnalysisEntity(
                incident,
                user,
                riskLevel,
                summary.toString().trim(),
                toJson(possibleCauses),
                toJson(investigationSteps),
                toJson(proposedActions),
                toJson(limitations)
        );

        entity = analysisRepository.save(entity);
        return toDTO(entity);
    }

    private double getLatestMetric(List<MetricSampleEntity> samples, MetricType type, double fallback) {
        return samples.stream()
                .filter(s -> s.getMetricType() == type)
                .findFirst()
                .map(MetricSampleEntity::getMetricValue)
                .orElse(fallback);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private <T> T fromJson(String json, TypeReference<T> ref, T fallback) {
        try {
            return objectMapper.readValue(json, ref);
        } catch (Exception e) {
            return fallback;
        }
    }

    private AnalysisResultDTO toDTO(IncidentAnalysisEntity entity) {
        List<String> causes = fromJson(entity.getPossibleCausesJson(), new TypeReference<>() {}, List.of());
        List<String> steps = fromJson(entity.getInvestigationStepsJson(), new TypeReference<>() {}, List.of());
        List<ProposedActionDTO> actions = fromJson(entity.getProposedActionsJson(), new TypeReference<>() {}, List.of());
        List<String> limitations = fromJson(entity.getLimitationsJson(), new TypeReference<>() {}, List.of());

        return new AnalysisResultDTO(
                entity.getId(),
                entity.getIncident().getId(),
                entity.getRequestedBy().getId(),
                entity.getRequestedBy().getFullName(),
                entity.getRiskLevel(),
                entity.getSummary(),
                causes,
                steps,
                actions,
                limitations,
                entity.getCreatedAt()
        );
    }
}
