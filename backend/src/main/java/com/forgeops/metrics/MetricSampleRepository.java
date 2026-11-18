package com.forgeops.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MetricSampleRepository extends JpaRepository<MetricSampleEntity, UUID> {

    List<MetricSampleEntity> findByServiceIdAndMetricTypeAndSampledAtAfterOrderBySampledAtDesc(
            UUID serviceId, MetricType type, Instant since);

    List<MetricSampleEntity> findByServiceIdAndEnvironmentIdAndSampledAtAfterOrderBySampledAtDesc(
            UUID serviceId, UUID environmentId, Instant since);

    List<MetricSampleEntity> findByServiceIdAndSampledAtAfterOrderBySampledAtDesc(
            UUID serviceId, Instant since);

    @Query("SELECT m FROM MetricSampleEntity m WHERE m.service.id = :sid AND m.metricType = :type ORDER BY m.sampledAt DESC")
    List<MetricSampleEntity> findLatestByServiceAndType(@Param("sid") UUID serviceId, @Param("type") MetricType type);
}
