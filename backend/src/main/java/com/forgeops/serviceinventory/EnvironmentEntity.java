package com.forgeops.serviceinventory;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "environments",
    uniqueConstraints = @UniqueConstraint(name = "uk_service_env", columnNames = {"service_id", "name"}))
@EntityListeners(AuditingEntityListener.class)
public class EnvironmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(name = "env_type", nullable = false, length = 30)
    private String envType;

    @Column(name = "region_label", nullable = false, length = 50)
    private String regionLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnvironmentStatus status = EnvironmentStatus.HEALTHY;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public EnvironmentEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ServiceEntity getService() { return service; }
    public void setService(ServiceEntity service) { this.service = service; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEnvType() { return envType; }
    public void setEnvType(String envType) { this.envType = envType; }
    public String getRegionLabel() { return regionLabel; }
    public void setRegionLabel(String regionLabel) { this.regionLabel = regionLabel; }
    public EnvironmentStatus getStatus() { return status; }
    public void setStatus(EnvironmentStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
