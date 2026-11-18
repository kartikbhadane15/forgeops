package com.forgeops.incident;

import com.forgeops.common.exception.BusinessRuleException;
import com.forgeops.common.exception.InvalidStateTransitionException;
import com.forgeops.common.exception.ResourceNotFoundException;
import com.forgeops.incident.dto.*;
import com.forgeops.serviceinventory.EnvironmentEntity;
import com.forgeops.serviceinventory.EnvironmentRepository;
import com.forgeops.serviceinventory.ServiceEntity;
import com.forgeops.serviceinventory.ServiceRepository;
import com.forgeops.user.UserEntity;
import com.forgeops.user.UserRepository;
import com.forgeops.user.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
public class IncidentLifecycleService {

    private final IncidentRepository incidentRepository;
    private final IncidentNoteRepository incidentNoteRepository;
    private final ServiceRepository serviceRepository;
    private final EnvironmentRepository environmentRepository;
    private final UserRepository userRepository;

    public IncidentLifecycleService(
            IncidentRepository incidentRepository,
            IncidentNoteRepository incidentNoteRepository,
            ServiceRepository serviceRepository,
            EnvironmentRepository environmentRepository,
            UserRepository userRepository
    ) {
        this.incidentRepository = incidentRepository;
        this.incidentNoteRepository = incidentNoteRepository;
        this.serviceRepository = serviceRepository;
        this.environmentRepository = environmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<IncidentSummaryDTO> listIncidents(
            IncidentStatus status,
            IncidentSeverity severity,
            UUID serviceId,
            Pageable pageable
    ) {
        return incidentRepository.findWithFilters(status, severity, serviceId, pageable)
                .map(this::toSummaryDTO);
    }

    @Transactional(readOnly = true)
    public IncidentDTO getIncident(UUID id) {
        IncidentEntity entity = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));
        List<IncidentNoteDTO> notes = incidentNoteRepository.findByIncidentIdOrderByCreatedAtAsc(id)
                .stream().map(this::toNoteDTO).toList();
        return toDTO(entity, notes);
    }

    @Transactional
    public IncidentDTO createIncident(CreateIncidentRequest req, UUID creatorId) {
        ServiceEntity service = serviceRepository.findById(req.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", req.serviceId()));

        EnvironmentEntity env = environmentRepository.findById(req.environmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Environment", "id", req.environmentId()));

        if (!env.getService().getId().equals(service.getId())) {
            throw new BusinessRuleException("Environment does not belong to the selected service");
        }

        UserEntity creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorId));

        String incidentNumber = generateIncidentNumber();
        IncidentEntity entity = new IncidentEntity(
                incidentNumber,
                req.title(),
                req.description(),
                service,
                env,
                req.severity(),
                IncidentStatus.OPEN,
                creator
        );

        entity = incidentRepository.save(entity);
        return toDTO(entity, List.of());
    }

    @Transactional
    public IncidentDTO transitionStatus(UUID incidentId, TransitionRequest req) {
        IncidentEntity entity = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", incidentId));

        validateTransition(entity.getStatus(), req.targetStatus());

        if (req.targetStatus() == IncidentStatus.RESOLVED) {
            if (req.resolutionSummary() == null || req.resolutionSummary().isBlank()) {
                throw new BusinessRuleException("Resolution summary is mandatory when marking an incident RESOLVED");
            }
            entity.setResolutionSummary(req.resolutionSummary());
        }

        entity.setStatus(req.targetStatus());
        entity = incidentRepository.save(entity);

        List<IncidentNoteDTO> notes = incidentNoteRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId)
                .stream().map(this::toNoteDTO).toList();
        return toDTO(entity, notes);
    }

    @Transactional
    public IncidentDTO assignIncident(UUID incidentId, UUID assigneeId) {
        IncidentEntity entity = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", incidentId));

        UserEntity assignee = null;
        if (assigneeId != null) {
            assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", assigneeId));
        }

        entity.setAssignee(assignee);
        entity = incidentRepository.save(entity);

        List<IncidentNoteDTO> notes = incidentNoteRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId)
                .stream().map(this::toNoteDTO).toList();
        return toDTO(entity, notes);
    }

    @Transactional
    public IncidentNoteDTO addNote(UUID incidentId, AddNoteRequest req, UUID authorId) {
        IncidentEntity incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", incidentId));

        UserEntity author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

        IncidentNoteEntity note = new IncidentNoteEntity(incident, author, req.noteBody());
        note = incidentNoteRepository.save(note);
        return toNoteDTO(note);
    }

    private synchronized String generateIncidentNumber() {
        int currentYear = Year.now().getValue();
        String prefix = "INC-" + currentYear + "-";
        Integer maxSeq = incidentRepository.findMaxIncidentSequenceNumber(prefix);
        int nextSeq = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, nextSeq);
    }

    private void validateTransition(IncidentStatus from, IncidentStatus to) {
        if (from == to) return;

        boolean allowed = switch (from) {
            case OPEN -> to == IncidentStatus.INVESTIGATING || to == IncidentStatus.RESOLVED;
            case INVESTIGATING -> to == IncidentStatus.MITIGATED || to == IncidentStatus.RESOLVED || to == IncidentStatus.OPEN;
            case MITIGATED -> to == IncidentStatus.RESOLVED || to == IncidentStatus.INVESTIGATING;
            case RESOLVED -> false; // Terminal state unless reopened via specific workflow
        };

        if (!allowed) {
            throw new InvalidStateTransitionException("Incident", from.name(), to.name());
        }
    }

    private IncidentSummaryDTO toSummaryDTO(IncidentEntity entity) {
        return new IncidentSummaryDTO(
                entity.getId(),
                entity.getIncidentNumber(),
                entity.getTitle(),
                entity.getService().getId(),
                entity.getService().getName(),
                entity.getEnvironment().getId(),
                entity.getEnvironment().getName(),
                entity.getSeverity(),
                entity.getStatus(),
                entity.getAssignee() != null ? toUserDTO(entity.getAssignee()) : null,
                toUserDTO(entity.getCreatedBy()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private IncidentDTO toDTO(IncidentEntity entity, List<IncidentNoteDTO> notes) {
        return new IncidentDTO(
                entity.getId(),
                entity.getIncidentNumber(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getService().getId(),
                entity.getService().getName(),
                entity.getEnvironment().getId(),
                entity.getEnvironment().getName(),
                entity.getSeverity(),
                entity.getStatus(),
                entity.getAssignee() != null ? toUserDTO(entity.getAssignee()) : null,
                toUserDTO(entity.getCreatedBy()),
                entity.getResolutionSummary(),
                notes,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private IncidentNoteDTO toNoteDTO(IncidentNoteEntity note) {
        return new IncidentNoteDTO(
                note.getId(),
                note.getIncident().getId(),
                note.getAuthor().getId(),
                note.getAuthor().getFullName(),
                note.getAuthor().getEmail(),
                note.getNoteBody(),
                note.getCreatedAt()
        );
    }

    private UserDTO toUserDTO(UserEntity user) {
        return UserDTO.fromEntity(user);
    }
}
