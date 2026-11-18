package com.forgeops.approval;

import com.forgeops.incident.IncidentEntity;
import com.forgeops.serviceinventory.ServiceEntity;
import com.forgeops.user.UserEntity;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "action_requests")
@EntityListeners(AuditingEntityListener.class)
public class ActionRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 64)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private IncidentEntity incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_service_id", nullable = false)
    private ServiceEntity targetService;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private ActionType actionType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 30)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING_APPROVAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private UserEntity requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private UserEntity reviewedBy;

    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by_id")
    private UserEntity executedBy;

    @Column(name = "execution_result", columnDefinition = "TEXT")
    private String executionResult;

    @CreatedDate
    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "executed_at")
    private Instant executedAt;

    public ActionRequestEntity() {}

    public ActionRequestEntity(String idempotencyKey, IncidentEntity incident, ServiceEntity targetService,
                               ActionType actionType, String reason, String riskLevel, UserEntity requestedBy) {
        this.idempotencyKey = idempotencyKey;
        this.incident = incident;
        this.targetService = targetService;
        this.actionType = actionType;
        this.reason = reason;
        this.riskLevel = riskLevel;
        this.requestedBy = requestedBy;
        this.approvalStatus = ApprovalStatus.PENDING_APPROVAL;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public IncidentEntity getIncident() { return incident; }
    public void setIncident(IncidentEntity incident) { this.incident = incident; }

    public ServiceEntity getTargetService() { return targetService; }
    public void setTargetService(ServiceEntity targetService) { this.targetService = targetService; }

    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }

    public UserEntity getRequestedBy() { return requestedBy; }
    public void setRequestedBy(UserEntity requestedBy) { this.requestedBy = requestedBy; }

    public UserEntity getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UserEntity reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }

    public UserEntity getExecutedBy() { return executedBy; }
    public void setExecutedBy(UserEntity executedBy) { this.executedBy = executedBy; }

    public String getExecutionResult() { return executionResult; }
    public void setExecutionResult(String executionResult) { this.executionResult = executionResult; }

    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }

    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }

    public Instant getExecutedAt() { return executedAt; }
    public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }
}
