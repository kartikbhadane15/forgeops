package com.forgeops.approval;

import com.forgeops.approval.dto.ActionRequestDTO;
import com.forgeops.approval.dto.CreateActionRequest;
import com.forgeops.approval.dto.ReviewActionRequest;
import com.forgeops.audit.AuditEventType;
import com.forgeops.audit.AuditTrailService;
import com.forgeops.common.exception.BusinessRuleException;
import com.forgeops.common.exception.InvalidStateTransitionException;
import com.forgeops.common.exception.ResourceNotFoundException;
import com.forgeops.incident.IncidentEntity;
import com.forgeops.incident.IncidentRepository;
import com.forgeops.serviceinventory.ServiceEntity;
import com.forgeops.serviceinventory.ServiceRepository;
import com.forgeops.user.UserEntity;
import com.forgeops.user.UserRepository;
import com.forgeops.user.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApprovalWorkflowService {

    private final ActionRequestRepository actionRequestRepository;
    private final IncidentRepository incidentRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final SafeSimulatedActionExecutor actionExecutor;
    private final AuditTrailService auditTrailService;

    public ApprovalWorkflowService(
            ActionRequestRepository actionRequestRepository,
            IncidentRepository incidentRepository,
            ServiceRepository serviceRepository,
            UserRepository userRepository,
            SafeSimulatedActionExecutor actionExecutor,
            AuditTrailService auditTrailService
    ) {
        this.actionRequestRepository = actionRequestRepository;
        this.incidentRepository = incidentRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.actionExecutor = actionExecutor;
        this.auditTrailService = auditTrailService;
    }

    @Transactional(readOnly = true)
    public Page<ActionRequestDTO> listRequests(ApprovalStatus status, UUID incidentId, Pageable pageable) {
        return actionRequestRepository.findWithFilters(status, incidentId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ActionRequestDTO getRequest(UUID id) {
        ActionRequestEntity entity = actionRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ActionRequest", "id", id));
        return toDTO(entity);
    }

    @Transactional
    public ActionRequestDTO createRequest(CreateActionRequest req, UUID requesterId) {
        // Idempotency check: if key exists, return existing record
        Optional<ActionRequestEntity> existing = actionRequestRepository.findByIdempotencyKey(req.idempotencyKey());
        if (existing.isPresent()) {
            return toDTO(existing.get());
        }

        IncidentEntity incident = incidentRepository.findById(req.incidentId())
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", req.incidentId()));

        ServiceEntity service = serviceRepository.findById(req.targetServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", req.targetServiceId()));

        UserEntity requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", requesterId));

        ActionRequestEntity entity = new ActionRequestEntity(
                req.idempotencyKey(),
                incident,
                service,
                req.actionType(),
                req.reason(),
                req.riskLevel(),
                requester
        );

        entity = actionRequestRepository.save(entity);

        auditTrailService.record(
                requesterId,
                AuditEventType.ACTION_REQUEST_CREATED,
                "ACTION_REQUEST",
                entity.getId().toString(),
                entity.getIdempotencyKey(),
                String.format("Action request '%s' created for service '%s'", entity.getActionType(), service.getName()),
                Map.of("actionType", entity.getActionType().name(), "riskLevel", entity.getRiskLevel())
        );

        return toDTO(entity);
    }

    @Transactional
    public ActionRequestDTO approveRequest(UUID requestId, ReviewActionRequest review, UUID reviewerId) {
        ActionRequestEntity entity = actionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ActionRequest", "id", requestId));

        if (entity.getApprovalStatus() != ApprovalStatus.PENDING_APPROVAL) {
            throw new InvalidStateTransitionException("ActionRequest", entity.getApprovalStatus().name(), ApprovalStatus.APPROVED.name());
        }

        // SEGREGATION OF DUTIES: Requester cannot approve their own request!
        if (entity.getRequestedBy().getId().equals(reviewerId)) {
            throw new BusinessRuleException("Segregation of duties violation: The requester cannot approve their own action request.");
        }

        UserEntity reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reviewerId));

        entity.setApprovalStatus(ApprovalStatus.APPROVED);
        entity.setReviewedBy(reviewer);
        entity.setReviewComment(review.comment());
        entity.setReviewedAt(Instant.now());

        entity = actionRequestRepository.save(entity);

        auditTrailService.record(
                reviewerId,
                AuditEventType.ACTION_REQUEST_APPROVED,
                "ACTION_REQUEST",
                entity.getId().toString(),
                entity.getIdempotencyKey(),
                String.format("Action request '%s' APPROVED by reviewer", entity.getActionType()),
                Map.of("comment", entity.getReviewComment() != null ? entity.getReviewComment() : "")
        );

        return toDTO(entity);
    }

    @Transactional
    public ActionRequestDTO rejectRequest(UUID requestId, ReviewActionRequest review, UUID reviewerId) {
        ActionRequestEntity entity = actionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ActionRequest", "id", requestId));

        if (entity.getApprovalStatus() != ApprovalStatus.PENDING_APPROVAL) {
            throw new InvalidStateTransitionException("ActionRequest", entity.getApprovalStatus().name(), ApprovalStatus.REJECTED.name());
        }

        if (review.comment() == null || review.comment().isBlank()) {
            throw new BusinessRuleException("A rejection reason/comment is mandatory when rejecting an action request.");
        }

        UserEntity reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reviewerId));

        entity.setApprovalStatus(ApprovalStatus.REJECTED);
        entity.setReviewedBy(reviewer);
        entity.setReviewComment(review.comment());
        entity.setReviewedAt(Instant.now());

        entity = actionRequestRepository.save(entity);

        auditTrailService.record(
                reviewerId,
                AuditEventType.ACTION_REQUEST_REJECTED,
                "ACTION_REQUEST",
                entity.getId().toString(),
                entity.getIdempotencyKey(),
                String.format("Action request '%s' REJECTED by reviewer", entity.getActionType()),
                Map.of("comment", entity.getReviewComment())
        );

        return toDTO(entity);
    }

    @Transactional
    public ActionRequestDTO executeRequest(UUID requestId, UUID executorId) {
        ActionRequestEntity entity = actionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ActionRequest", "id", requestId));

        // Replay protection: if already executed, return current state
        if (entity.getApprovalStatus() == ApprovalStatus.EXECUTED) {
            return toDTO(entity);
        }

        // Must be in APPROVED status to execute
        if (entity.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new BusinessRuleException(String.format("Cannot execute action request with status '%s'. Only APPROVED requests can be executed.",
                    entity.getApprovalStatus()));
        }

        UserEntity executor = userRepository.findById(executorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", executorId));

        try {
            String outcome = actionExecutor.execute(entity.getActionType(), entity.getTargetService().getId());
            entity.setApprovalStatus(ApprovalStatus.EXECUTED);
            entity.setExecutedBy(executor);
            entity.setExecutionResult(outcome);
            entity.setExecutedAt(Instant.now());
        } catch (Exception ex) {
            entity.setApprovalStatus(ApprovalStatus.FAILED);
            entity.setExecutedBy(executor);
            entity.setExecutionResult("EXECUTION FAILED: " + ex.getMessage());
            entity.setExecutedAt(Instant.now());
        }

        entity = actionRequestRepository.save(entity);

        auditTrailService.record(
                executorId,
                AuditEventType.ACTION_REQUEST_EXECUTED,
                "ACTION_REQUEST",
                entity.getId().toString(),
                entity.getIdempotencyKey(),
                String.format("Action '%s' executed against service '%s' with outcome: %s",
                        entity.getActionType(), entity.getTargetService().getName(), entity.getApprovalStatus()),
                Map.of("status", entity.getApprovalStatus().name(), "result", entity.getExecutionResult())
        );

        return toDTO(entity);
    }

    private ActionRequestDTO toDTO(ActionRequestEntity entity) {
        return new ActionRequestDTO(
                entity.getId(),
                entity.getIdempotencyKey(),
                entity.getIncident().getId(),
                entity.getIncident().getIncidentNumber(),
                entity.getIncident().getTitle(),
                entity.getTargetService().getId(),
                entity.getTargetService().getName(),
                entity.getActionType(),
                entity.getReason(),
                entity.getRiskLevel(),
                entity.getApprovalStatus(),
                UserDTO.fromEntity(entity.getRequestedBy()),
                entity.getReviewedBy() != null ? UserDTO.fromEntity(entity.getReviewedBy()) : null,
                entity.getReviewComment(),
                entity.getExecutedBy() != null ? UserDTO.fromEntity(entity.getExecutedBy()) : null,
                entity.getExecutionResult(),
                entity.getRequestedAt(),
                entity.getReviewedAt(),
                entity.getExecutedAt()
        );
    }
}
