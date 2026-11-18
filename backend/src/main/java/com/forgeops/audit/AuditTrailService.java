package com.forgeops.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgeops.audit.dto.AuditEventDTO;
import com.forgeops.user.UserEntity;
import com.forgeops.user.UserRepository;
import com.forgeops.user.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class AuditTrailService {

    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditTrailService(
            AuditEventRepository auditEventRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.auditEventRepository = auditEventRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
            UUID actorUserId,
            AuditEventType eventType,
            String entityType,
            String entityId,
            String correlationId,
            String safeSummary,
            Map<String, Object> details
    ) {
        UserEntity actor = null;
        if (actorUserId != null) {
            actor = userRepository.findById(actorUserId).orElse(null);
        }

        String detailsJson = null;
        if (details != null && !details.isEmpty()) {
            try {
                detailsJson = objectMapper.writeValueAsString(details);
            } catch (Exception ignored) {}
        }

        AuditEventEntity event = new AuditEventEntity(
                actor,
                eventType.name(),
                entityType,
                entityId,
                correlationId,
                safeSummary,
                detailsJson
        );

        auditEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Page<AuditEventDTO> listEvents(String eventType, String entityType, String entityId, Pageable pageable) {
        return auditEventRepository.findWithFilters(eventType, entityType, entityId, pageable)
                .map(this::toDTO);
    }

    private AuditEventDTO toDTO(AuditEventEntity entity) {
        UserDTO actorDTO = entity.getActor() != null ? UserDTO.fromEntity(entity.getActor()) : null;
        return new AuditEventDTO(
                entity.getId(),
                actorDTO,
                entity.getEventType(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getCorrelationId(),
                entity.getSafeSummary(),
                entity.getDetailsJson(),
                entity.getCreatedAt()
        );
    }
}
