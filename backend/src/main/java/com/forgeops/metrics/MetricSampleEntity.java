package com.forgeops.metrics;

import com.forgeops.serviceinventory.EnvironmentEntity;
import com.forgeops.serviceinventory.ServiceEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "metric_samples")
public class MetricSampleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "environment_id", nullable = false)
    private EnvironmentEntity environment;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 30)
    private MetricType metricType;

    @Column(name = "metric_value", nullable = false)
    private double metricValue;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "sampled_at", nullable = false)
    private Instant sampledAt = Instant.now();

    public MetricSampleEntity() {}

    public MetricSampleEntity(ServiceEntity svc, EnvironmentEntity env, MetricType type, double value, String unit) {
        this.service = svc;
        this.environment = env;
        this.metricType = type;
        this.metricValue = value;
        this.unit = unit;
        this.sampledAt = Instant.now();
    }

    public UUID getId() { return id; }
    public ServiceEntity getService() { return service; }
    public void setService(ServiceEntity service) { this.service = service; }
    public EnvironmentEntity getEnvironment() { return environment; }
    public void setEnvironment(EnvironmentEntity environment) { this.environment = environment; }
    public MetricType getMetricType() { return metricType; }
    public void setMetricType(MetricType metricType) { this.metricType = metricType; }
    public double getMetricValue() { return metricValue; }
    public void setMetricValue(double metricValue) { this.metricValue = metricValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Instant getSampledAt() { return sampledAt; }
    public void setSampledAt(Instant sampledAt) { this.sampledAt = sampledAt; }
}
