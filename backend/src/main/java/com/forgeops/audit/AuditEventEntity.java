package com.forgeops.audit;

import com.forgeops.user.UserEntity;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
@EntityListeners(AuditingEntityListener.class)
public class AuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private UserEntity actor;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "entity_type", nullable = false, length = 64)
    private String entityType;

    @Column(name = "entity_id", nullable = false, length = 64)
    private String entityId;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "safe_summary", nullable = false, columnDefinition = "TEXT")
    private String safeSummary;

    @Column(name = "details", columnDefinition = "TEXT")
    private String detailsJson;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AuditEventEntity() {}

    public AuditEventEntity(UserEntity actor, String eventType, String entityType,
                            String entityId, String correlationId, String safeSummary, String detailsJson) {
        this.actor = actor;
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.correlationId = correlationId;
        this.safeSummary = safeSummary;
        this.detailsJson = detailsJson;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UserEntity getActor() { return actor; }
    public void setActor(UserEntity actor) { this.actor = actor; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getSafeSummary() { return safeSummary; }
    public void setSafeSummary(String safeSummary) { this.safeSummary = safeSummary; }

    public String getDetailsJson() { return detailsJson; }
    public void setDetailsJson(String detailsJson) { this.detailsJson = detailsJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
