package com.forgeops.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgeops.ai.dto.AnalysisResultDTO;
import com.forgeops.incident.IncidentEntity;
import com.forgeops.incident.IncidentRepository;
import com.forgeops.incident.IncidentSeverity;
import com.forgeops.incident.IncidentStatus;
import com.forgeops.metrics.MetricSampleEntity;
import com.forgeops.metrics.MetricSampleRepository;
import com.forgeops.metrics.MetricType;
import com.forgeops.serviceinventory.EnvironmentEntity;
import com.forgeops.serviceinventory.EnvironmentStatus;
import com.forgeops.serviceinventory.ServiceEntity;
import com.forgeops.user.UserEntity;
import com.forgeops.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleBasedIncidentAnalysisServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentAnalysisRepository analysisRepository;

    @Mock
    private MetricSampleRepository metricSampleRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RuleBasedIncidentAnalysisService analysisService;

    private IncidentEntity testIncident;
    private UserEntity testUser;
    private ServiceEntity testService;
    private EnvironmentEntity testEnv;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity("engineer@forgeops.local", "hashed", "Test Engineer");
        testUser.setId(UUID.randomUUID());

        testService = new ServiceEntity();
        testService.setId(UUID.randomUUID());
        testService.setServiceKey("orders-api");
        testService.setName("Orders Service");
        testService.setOwningTeam("Backend");
        testService.setCriticality(com.forgeops.serviceinventory.Criticality.CRITICAL);

        testEnv = new EnvironmentEntity();
        testEnv.setId(UUID.randomUUID());
        testEnv.setService(testService);
        testEnv.setName("Production");
        testEnv.setEnvType("PROD");
        testEnv.setRegionLabel("us-east-1");
        testEnv.setStatus(EnvironmentStatus.DEGRADED);

        testIncident = new IncidentEntity(
                "INC-2026-0001",
                "High latency on checkout",
                "Users reporting slow responses",
                testService,
                testEnv,
                IncidentSeverity.SEV2,
                IncidentStatus.INVESTIGATING,
                testUser
        );
        testIncident.setId(UUID.randomUUID());
    }

    @Test
    void shouldTriggerHighLatencyRuleWhenP95ExceedsThreshold() {
        UUID incidentId = testIncident.getId();
        UUID userId = testUser.getId();

        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(testIncident));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        MetricSampleEntity latencySample = new MetricSampleEntity(
                testService, testEnv, MetricType.LATENCY_P95, 1250.0, "ms"
        );
        when(metricSampleRepository.findByServiceIdAndSampledAtAfterOrderBySampledAtDesc(any(), any()))
                .thenReturn(List.of(latencySample));

        when(analysisRepository.save(any(IncidentAnalysisEntity.class))).thenAnswer(invocation -> {
            IncidentAnalysisEntity saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            saved.setCreatedAt(Instant.now());
            return saved;
        });

        AnalysisResultDTO result = analysisService.runAnalysis(incidentId, userId);

        assertNotNull(result);
        assertEquals("MEDIUM", result.riskLevel());
        assertTrue(result.summary().contains("P95 latency is elevated"));
        assertFalse(result.proposedActions().isEmpty());
        assertEquals("RESTART_DEMO_SERVICE", result.proposedActions().get(0).actionType());
        verify(analysisRepository).save(any(IncidentAnalysisEntity.class));
    }

    @Test
    void shouldTriggerUnavailableRuleWhenEnvironmentIsUnavailable() {
        UUID incidentId = testIncident.getId();
        UUID userId = testUser.getId();
        testEnv.setStatus(EnvironmentStatus.UNAVAILABLE);

        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(testIncident));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(metricSampleRepository.findByServiceIdAndSampledAtAfterOrderBySampledAtDesc(any(), any()))
                .thenReturn(List.of());

        when(analysisRepository.save(any(IncidentAnalysisEntity.class))).thenAnswer(invocation -> {
            IncidentAnalysisEntity saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            saved.setCreatedAt(Instant.now());
            return saved;
        });

        AnalysisResultDTO result = analysisService.runAnalysis(incidentId, userId);

        assertNotNull(result);
        assertEquals("HIGH", result.riskLevel());
        assertTrue(result.summary().contains("UNAVAILABLE"));
        assertEquals(2, result.proposedActions().size());
    }
}
