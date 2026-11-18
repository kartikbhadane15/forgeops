# ForgeOps Phased Development Roadmap

This roadmap governs the incremental implementation of ForgeOps. Each phase must meet its designated verification gate before proceeding to the subsequent phase.

---

## Phase Overview & Progress Tracker

- [x] **Phase 0: Planning & Design**
  - Architecture specification (`docs/architecture.md`)
  - ER diagram & schema design (`docs/database-design.md`)
  - REST API specification (`docs/api-contract.md`)
  - Security & RBAC design (`docs/security.md`)
  - AI Safety & Deterministic rules specification (`docs/ai-safety.md`)
  - Threat model & STRIDE matrix (`docs/threat-model.md`)
  - Testing strategy & DoD (`docs/testing-strategy.md`)
  - Implementation roadmap (`docs/development-roadmap.md`)
  - **Gate:** Architecture and data schemas are internally consistent and approved by developer.

- [ ] **Phase 1: Project Foundation & Infrastructure**
  - Spring Boot 3.x project setup (Java 21, Maven wrapper, Spring Web, Data JPA, Security, Actuator, Flyway).
  - React SPA setup with Vite, Tailwind CSS, Lucide icons, React Router.
  - Docker Compose configuration for PostgreSQL.
  - Flyway initial migration scripts (`V1__init_schema.sql`).
  - Base configuration, environment variables (`.env.example`), Actuator health endpoint.
  - Central exception handler (`GlobalExceptionHandler`) and standardized `ApiResponse` envelope.
  - **Gate:** PostgreSQL container, Spring Boot backend, and Vite frontend run locally and respond to health checks.

- [ ] **Phase 2: Authentication & Access Control (RBAC)**
  - Database schema for users, roles, user-roles.
  - Spring Security filter chain with JWT token provider and authentication filter.
  - Seed users with BCrypt-hashed passwords for all 4 roles.
  - `POST /api/v1/auth/login`, `POST /api/v1/auth/logout`, `GET /api/v1/auth/me`.
  - Admin user management endpoints (`/api/v1/admin/users`).
  - React auth context, login view, session storage, and role-guarded routes.
  - **Gate:** Automated tests prove that unauthenticated/unauthorized requests receive HTTP 401/403.

- [ ] **Phase 3: Service Inventory & Operational Dashboard**
  - Services and environments entities and repositories.
  - Synthetic metric samples generator and health calculation engine.
  - Service CRUD endpoints with pagination, search, and filtering.
  - Dashboard summary endpoint (`/api/v1/dashboard/summary`, `service-health`, `recent-incidents`).
  - Frontend Dashboard screen (summary cards, health table, Recharts latency/error chart).
  - Frontend Services screen (filterable service table, service details view).
  - **Gate:** Dashboard and service inventory display real database records from backend APIs.

- [ ] **Phase 4: Incident Management Lifecycle**
  - Incidents and incident notes entities, repositories, and state transition logic.
  - Endpoints: Create incident, get details, add investigation notes, assign, transition status.
  - Strict validation of transitions and required `resolutionSummary` on `RESOLVED`.
  - Frontend Incidents list with severity and status filters.
  - Frontend Incident Detail page with timeline, investigation notes editor, and status updater.
  - **Gate:** Complete incident lifecycle verified with unit and integration tests.

- [ ] **Phase 5: Deterministic AI Incident Assistant**
  - Local rule-based analysis service (`IncidentAnalysisService`).
  - Deterministic evaluation of incident severity, description keywords, and recent metric anomalies.
  - Generation of structured analysis DTOs (summary, causes, investigation steps, proposed actions, risk, limitations).
  - Persistence of analyses in `ai_analyses` table.
  - Frontend AI Assistant panel with clear "Rule-Based Advisory" labeling and action proposal card.
  - **Gate:** Analysis executes 100% locally and offline without external API keys or network calls.

- [ ] **Phase 6: Human Approval & Safe Action Execution**
  - Action request entity, repository, and workflow service.
  - Action approval endpoints: create request, approve, reject (with mandatory reason).
  - Enforce dual-control invariant: requester cannot approve their own action request.
  - Safe simulated action executor for `MARK_SERVICE_MAINTENANCE`, `CLEAR_DEMO_ALERT`, `RESTART_DEMO_SERVICE`.
  - Idempotency protection against duplicate execution.
  - Frontend Approvals Inbox screen with approve/reject modal and execution controls.
  - **Gate:** Actions cannot execute without valid prior approval by an independent approver.

- [ ] **Phase 7: Audit Trail, Observability & Quality Assurance**
  - Append-only `audit_events` entity and service logging all critical state changes.
  - Audit trail viewing endpoint and frontend Audit Log screen.
  - Correlation ID tracking and structured logging.
  - Comprehensive unit and integration test suite (coverage of self-approval prevention, invalid transitions, executor safety).
  - Frontend automated tests for forms and route guards.
  - **Gate:** All tests pass, audit log captures end-to-end lifecycle, Actuator metrics operational.

- [ ] **Phase 8: Portfolio Polish & Verification**
  - Comprehensive root `README.md` with architecture diagrams, demo credentials, prerequisites, and startup guides.
  - Seed data verification for the 13-step end-to-end demo scenario.
  - Clean clone verification instructions and CI workflow (`.github/workflows/ci.yml`).
  - **Gate:** A reviewer can follow README steps from scratch, log in as any role, and complete the full scenario.

- [ ] **Phase 9: Production Deployment Readiness**
  - Dockerfiles for backend and frontend.
  - Multi-stage build optimization.
  - Production environment configurations and HTTPS/CORS documentation.
  - **Gate:** Production container images build and run cleanly.
