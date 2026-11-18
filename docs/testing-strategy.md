# ForgeOps Testing Strategy & Quality Assurance Plan

## 1. Testing Philosophy

Testing in ForgeOps is treated as a first-class deliverable across all phases. Every core business rule, state transition, security check, and UI interaction must be validated with automated tests.

---

## 2. Test Classification & Scope

### 2.1 Backend Unit Tests (JUnit 5 + Mockito)
- **Domain & Service Invariants**:
  - `IncidentLifecycleServiceTest`:
    - Valid transitions: `OPEN` -> `INVESTIGATING` -> `MITIGATED` -> `RESOLVED`.
    - Invalid transitions: Attempting to resolve without a `resolutionSummary` must throw `ValidationException`.
    - Illegal reverse transitions (e.g. `RESOLVED` -> `OPEN` without admin privileges).
  - `ApprovalWorkflowServiceTest`:
    - Requester cannot approve their own action request (`SELF_APPROVAL_PROHIBITED`).
    - Approver cannot approve an already `APPROVED` or `REJECTED` request.
    - Rejection requires a non-blank reason.
    - Non-approved actions (`PENDING_APPROVAL`, `REJECTED`) throw an exception when execution is attempted.
  - `DeterministicRuleAnalysisServiceTest`:
    - Latency spike + error rate triggers `RESTART_DEMO_SERVICE` recommendation.
    - High memory utilization triggers memory exhaustion warning and restart proposal.
    - Empty or insufficient telemetry samples gracefully returns safe fallback with limitation disclosures.
    - Zero external network calls made.
  - `SafeSimulatedActionExecutorTest`:
    - Validates execution produces expected state changes on simulated targets.
    - Enforces idempotency when called repeatedly.

### 2.2 Backend Integration Tests (Spring Boot Test + MockMvc + PostgreSQL/Testcontainers)
- **Security & Authorization**:
  - Unauthenticated requests to protected endpoints return `401 Unauthorized`.
  - Authenticated user with `ROLE_VIEWER` attempting to create an incident returns `403 Forbidden`.
  - Authenticated user with `ROLE_ENGINEER` attempting to approve an action returns `403 Forbidden`.
- **End-to-End API Workflows**:
  - Full happy path: Login -> Create Incident -> Request AI Analysis -> Submit Action Request -> Login as Approver -> Approve Action -> Login as Engineer -> Execute Action -> Verify Audit Trail.
  - Flyway migration verification: Asserts migrations apply cleanly and maintain schema constraints.

### 2.3 Frontend Tests (Vitest + React Testing Library)
- **Form Validation**:
  - Login form requires valid email format and non-empty password.
  - Incident creation modal enforces title, severity selection, and service selection.
  - Action rejection modal requires mandatory comment before enabling the reject button.
- **Route Guards & Role Visibility**:
  - Unauthenticated users are redirected to `/login`.
  - `Admin` navigation tab is hidden for `VIEWER`, `ENGINEER`, and `APPROVER`.
  - Approval action buttons are disabled or hidden if current user is the requester (preventing self-approval in the UI).
- **Accessibility & Keyboard Navigation**:
  - Focus trapping in modals and dialogs.
  - Screen reader announcements for alert states and status badges.

---

## 3. Definition of Done (DoD)

A feature or phase is considered **DONE** only when:
1. **Happy Path Validated**: All functional requirements in the user story operate end-to-end.
2. **Edge Cases & Error Handling**: Invalid input, boundary constraints, and error conditions return clear standardized error envelopes.
3. **Security Invariants Enforced**: Server-side role checks reject unauthorized attempts with `401` or `403`.
4. **Audit Logging Verified**: Relevant state transitions emit structured audit records.
5. **UI States Complete**: The frontend handles Loading, Success, Empty, and Error states cleanly.
6. **Automated Tests Passing**: All unit and integration tests compile and pass in the build.
7. **Documentation Current**: Any API, schema, or configuration updates are reflected in `docs/`.
