package com.forgeops.approval;

import com.forgeops.approval.dto.ActionRequestDTO;
import com.forgeops.approval.dto.ReviewActionRequest;
import com.forgeops.common.exception.BusinessRuleException;
import com.forgeops.incident.IncidentEntity;
import com.forgeops.incident.IncidentRepository;
import com.forgeops.serviceinventory.ServiceEntity;
import com.forgeops.serviceinventory.ServiceRepository;
import com.forgeops.user.UserEntity;
import com.forgeops.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalWorkflowServiceTest {

    @Mock
    private ActionRequestRepository actionRequestRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SafeSimulatedActionExecutor actionExecutor;

    @InjectMocks
    private ApprovalWorkflowService approvalWorkflowService;

    private UserEntity engineer;
    private UserEntity approver;
    private ServiceEntity service;
    private IncidentEntity incident;
    private ActionRequestEntity actionRequest;

    @BeforeEach
    void setUp() {
        engineer = new UserEntity("engineer@forgeops.local", "hash", "Ops Engineer");
        engineer.setId(UUID.randomUUID());

        approver = new UserEntity("approver@forgeops.local", "hash", "Sec Approver");
        approver.setId(UUID.randomUUID());

        service = new ServiceEntity();
        service.setId(UUID.randomUUID());
        service.setName("orders-api");

        incident = new IncidentEntity();
        incident.setId(UUID.randomUUID());
        incident.setIncidentNumber("INC-2026-0001");
        incident.setTitle("Outage");

        actionRequest = new ActionRequestEntity(
                "key-123",
                incident,
                service,
                ActionType.RESTART_DEMO_SERVICE,
                "Memory leak",
                "MEDIUM",
                engineer
        );
        actionRequest.setId(UUID.randomUUID());
    }

    @Test
    void shouldBlockSelfApprovalViolatingSegregationOfDuties() {
        UUID reqId = actionRequest.getId();
        when(actionRequestRepository.findById(reqId)).thenReturn(Optional.of(actionRequest));

        ReviewActionRequest review = new ReviewActionRequest("Self approval attempt");

        // Engineer attempts to approve own action request
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                approvalWorkflowService.approveRequest(reqId, review, engineer.getId())
        );

        assertTrue(ex.getMessage().contains("Segregation of duties violation"));
        verify(actionRequestRepository, never()).save(any());
    }

    @Test
    void shouldAllowAuthorizedApproverToApproveRequest() {
        UUID reqId = actionRequest.getId();
        when(actionRequestRepository.findById(reqId)).thenReturn(Optional.of(actionRequest));
        when(userRepository.findById(approver.getId())).thenReturn(Optional.of(approver));
        when(actionRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ReviewActionRequest review = new ReviewActionRequest("Looks good to proceed");

        ActionRequestDTO result = approvalWorkflowService.approveRequest(reqId, review, approver.getId());

        assertNotNull(result);
        assertEquals(ApprovalStatus.APPROVED, result.approvalStatus());
        assertEquals("Looks good to proceed", result.reviewComment());
        assertEquals(approver.getFullName(), result.reviewedBy().getFullName());
    }

    @Test
    void shouldExecuteApprovedActionSuccessfully() {
        UUID reqId = actionRequest.getId();
        actionRequest.setApprovalStatus(ApprovalStatus.APPROVED);

        when(actionRequestRepository.findById(reqId)).thenReturn(Optional.of(actionRequest));
        when(userRepository.findById(engineer.getId())).thenReturn(Optional.of(engineer));
        when(actionExecutor.execute(ActionType.RESTART_DEMO_SERVICE, service.getId()))
                .thenReturn("SUCCESS: Service restarted");
        when(actionRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ActionRequestDTO result = approvalWorkflowService.executeRequest(reqId, engineer.getId());

        assertEquals(ApprovalStatus.EXECUTED, result.approvalStatus());
        assertTrue(result.executionResult().contains("SUCCESS"));
        verify(actionExecutor).execute(ActionType.RESTART_DEMO_SERVICE, service.getId());
    }
}
