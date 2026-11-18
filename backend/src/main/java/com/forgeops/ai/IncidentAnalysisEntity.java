package com.forgeops.ai;

import com.forgeops.incident.IncidentEntity;
import com.forgeops.user.UserEntity;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_analyses")
@EntityListeners(AuditingEntityListener.class)
public class IncidentAnalysisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private IncidentEntity incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private UserEntity requestedBy;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "possible_causes", nullable = false, columnDefinition = "TEXT")
    private String possibleCausesJson;

    @Column(name = "investigation_steps", nullable = false, columnDefinition = "TEXT")
    private String investigationStepsJson;

    @Column(name = "proposed_actions", nullable = false, columnDefinition = "TEXT")
    private String proposedActionsJson;

    @Column(name = "limitations", nullable = false, columnDefinition = "TEXT")
    private String limitationsJson;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public IncidentAnalysisEntity() {}

    public IncidentAnalysisEntity(IncidentEntity incident, UserEntity requestedBy, String riskLevel,
                                  String summary, String possibleCausesJson, String investigationStepsJson,
                                  String proposedActionsJson, String limitationsJson) {
        this.incident = incident;
        this.requestedBy = requestedBy;
        this.riskLevel = riskLevel;
        this.summary = summary;
        this.possibleCausesJson = possibleCausesJson;
        this.investigationStepsJson = investigationStepsJson;
        this.proposedActionsJson = proposedActionsJson;
        this.limitationsJson = limitationsJson;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public IncidentEntity getIncident() { return incident; }
    public void setIncident(IncidentEntity incident) { this.incident = incident; }

    public UserEntity getRequestedBy() { return requestedBy; }
    public void setRequestedBy(UserEntity requestedBy) { this.requestedBy = requestedBy; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getPossibleCausesJson() { return possibleCausesJson; }
    public void setPossibleCausesJson(String possibleCausesJson) { this.possibleCausesJson = possibleCausesJson; }

    public String getInvestigationStepsJson() { return investigationStepsJson; }
    public void setInvestigationStepsJson(String investigationStepsJson) { this.investigationStepsJson = investigationStepsJson; }

    public String getProposedActionsJson() { return proposedActionsJson; }
    public void setProposedActionsJson(String proposedActionsJson) { this.proposedActionsJson = proposedActionsJson; }

    public String getLimitationsJson() { return limitationsJson; }
    public void setLimitationsJson(String limitationsJson) { this.limitationsJson = limitationsJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
