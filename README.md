# ForgeOps — Enterprise Infrastructure & Operational AI Platform

> **A production-grade, full-stack infrastructure operations and incident remediation platform with deterministic local AI analysis, human verification gates, and immutable audit governance.**

---

## 1. Overview & Architectural Philosophy

Modern enterprise operations require rapid response to telemetry anomalies without sacrificing safety, compliance, or human accountability. **ForgeOps** implements a modular, resilient operational monolith designed according to strict enterprise safety principles:

1. **Air-Gapped & Deterministic Local AI Engine**:
   - 100% offline rule-based heuristic inference engine running directly within the Spring Boot JVM.
   - Zero reliance on external LLMs, cloud APIs, third-party network egress, or subscription keys.
   - Immune to third-party outages, rate limits, network partitions, or hallucinated commands.
2. **Untrusted Recommendation Principle**:
   - Automated analysis recommendations are strictly classified as **advisory proposals**.
   - The AI engine has zero direct write authority over databases, servers, or cloud infrastructure.
3. **Mandatory Human-in-the-Loop & Segregation of Duties**:
   - Proposed actions require an engineer to create a formal `ActionRequest`.
   - Approvers must be separate individuals (**self-approval is cryptographically and logically blocked** at the backend).
   - Executable actions are confined to a strict server-side whitelist (`MARK_SERVICE_MAINTENANCE`, `CLEAR_DEMO_ALERT`, `RESTART_DEMO_SERVICE`).
4. **Immutable Audit Trail**:
   - Append-only, database-enforced compliance logging for all authentication attempts, incident state transitions, approvals, and action executions.

---

## 2. System Architecture

```mermaid
graph TD
    subgraph Client Layer [Frontend - Vite + React 18 + Tailwind]
        UI[Operations Dashboard & Control Center]
        AuthCtx[Auth Context & RBAC Guard]
    end

    subgraph API Gateway & Security [Spring Boot 3.3.5]
        Filter[JwtAuthenticationFilter]
        SecConfig[Spring Security RBAC Rules]
    end

    subgraph Core Monolith Services [Java 21 Monolith]
        AuthSvc[AuthService]
        IncSvc[IncidentLifecycleService]
        AISvc[RuleBasedIncidentAnalysisService]
        ApprSvc[ApprovalWorkflowService]
        ExecSvc[SafeSimulatedActionExecutor]
        AuditSvc[AuditTrailService]
    end

    subgraph Data & Persistence [PostgreSQL 16]
        DB_Users[(users & user_roles)]
        DB_Services[(services & environments)]
        DB_Metrics[(metric_samples)]
        DB_Incidents[(incidents & incident_notes)]
        DB_AI[(ai_analyses)]
        DB_Actions[(action_requests)]
        DB_Audit[(audit_events - Append Only)]
    end

    UI -->|REST with Bearer JWT| Filter
    Filter --> SecConfig
    SecConfig --> Core Monolith Services
    IncSvc --> DB_Incidents
    AISvc -->|Offline Metric Telemetry| DB_Metrics
    AISvc --> DB_AI
    ApprSvc -->|Enforce Segregation of Duties| DB_Actions
    ExecSvc -->|Safe Whitelist Mutations| DB_Services
    AuditSvc -->|RequiresNew TX| DB_Audit
```

---

## 3. Technology Stack

- **Backend**:
  - **Language**: Java 21 (Temurin OpenJDK)
  - **Framework**: Spring Boot 3.3.5
  - **Security**: Spring Security 6 with JJWT (0.12.6) Bearer tokens & BCrypt password hashing
  - **Data Access**: Spring Data JPA & Hibernate 6
  - **Database Migration**: Flyway (11 schema tables with index optimizations)
  - **API Documentation**: SpringDoc OpenAPI / Swagger 3 UI
- **Frontend**:
  - **Framework**: React 18 (SPA)
  - **Build Tool**: Vite 5
  - **Styling**: Tailwind CSS (Dark Enterprise Palette)
  - **Icons**: Lucide React
  - **HTTP Client**: Axios with automated bearer interceptors
- **Infrastructure**:
  - **Database**: PostgreSQL 16
  - **Containers**: Docker & Docker Compose

---

## 4. Role-Based Access Control (RBAC) Matrix

| Capability | VIEWER | ENGINEER | APPROVER | ADMIN |
|---|:---:|:---:|:---:|:---:|
| View Dashboard & Metrics | ✅ | ✅ | ✅ | ✅ |
| View Service Catalog | ✅ | ✅ | ✅ | ✅ |
| Declare Incident | ❌ | ✅ | ✅ | ✅ |
| Post Investigation Notes | ❌ | ✅ | ✅ | ✅ |
| Run Automated AI Advisory | ❌ | ✅ | ✅ | ✅ |
| Propose Remediation Action | ❌ | ✅ | ✅ | ✅ |
| Approve Action Requests | ❌ | ❌ | ✅ | ✅ |
| Execute Approved Actions | ❌ | ✅ | ✅ | ✅ |
| View Compliance Audit Logs | ❌ | ❌ | ✅ | ✅ |
| Administer Users & Roles | ❌ | ❌ | ❌ | ✅ |

---

## 5. Quick-Start Guide

### Prerequisites
- **Git**
- **Docker & Docker Compose**
- **Java 21 JDK**
- **Node.js 18+ & npm**

### 1. Clone & Start Database
```bash
git clone https://github.com/your-username/forgeops.git
cd forgeops

# Start PostgreSQL 16 container
docker compose up -d postgres
```

### 2. Run Backend
```bash
cd backend
./mvnw spring-boot:run
```
The backend initializes automatically:
- Flyway executes schema migrations (`V1__init_schema.sql`).
- `DataInitializer` seeds demo roles, users, 5 core microservices, environments, and telemetry samples.
- REST API available at `http://localhost:8080/api/v1`
- Swagger UI available at `http://localhost:8080/swagger-ui.html`

### 3. Run Frontend
```bash
cd ../frontend
npm install
npm run dev
```
Access the web console at: `http://localhost:5173`

---

## 6. Demo Accounts (All pre-seeded)

All accounts share the default password: **`Password123!`**

| Email | Role | Recommended Use Case |
|---|---|---|
| `viewer@forgeops.local` | `ROLE_VIEWER` | Read-only stakeholder monitoring |
| `engineer@forgeops.local` | `ROLE_ENGINEER` | Triage incidents, run AI analysis, propose actions, execute approved tasks |
| `approver@forgeops.local` | `ROLE_APPROVER` | Inspect action justifications, review proposals, enforce segregation of duties |
| `admin@forgeops.local` | `ROLE_ADMIN` | Global administrative governance, audit compliance review |

---

## 7. End-to-End Walkthrough Scenario

Experience the complete incident triage, deterministic advisory, and human-in-the-loop remediation lifecycle in 5 minutes:

### Step 1: Detect Degraded Service
1. Log in as **`engineer@forgeops.local`** (`Password123!`).
2. Notice that the **Orders API (`orders-api`)** is reporting high latency (P95 > 1200ms) with a `DEGRADED` health badge.

### Step 2: Declare an Incident
1. Navigate to **Incident Management** (`/incidents`) and click **"Declare Incident"**.
2. Select **`orders-api`** in **`Production`** with severity **`SEV2 - Major Degradation`**.
3. Title: `"Elevated checkout latency and thread saturation"`.
4. Click **Create Incident**.

### Step 3: Run Deterministic AI Advisory
1. Click into the new incident detail view (`/incidents/INC-2026-0001`).
2. Click **"Start Investigating"** and assign the incident to yourself.
3. In the **Rule-Based Operational Advisory** panel, click **"Run Advisory Analysis"**.
4. The local engine immediately evaluates recent metric telemetry, detects P95 latency > 800ms, and infers:
   - *Root Cause*: Downstream latency or database connection pool backlog.
   - *Investigation Steps*: Inspect active thread dumps and slow query logs.
   - *Proposed Action*: `RESTART_DEMO_SERVICE` (Medium Risk).

### Step 4: Propose Remediation Action
1. On the proposed action card, click **"Propose Action"**.
2. The request is submitted to the central queue with status `PENDING_APPROVAL`.
3. Try approving the action yourself: **The system blocks self-approval** to protect against unauthorized unilateral production mutations.

### Step 5: Segregation of Duties Review & Execution
1. Log out and log in as **`approver@forgeops.local`** (`Password123!`).
2. Navigate to **Approvals** (`/approvals`).
3. Click **"Approve"** on the pending request, add review notes: `"Latency verified in APM. Approved rolling restart."`
4. The status transitions to **`APPROVED`**.
5. Click **"Execute"**. The `SafeSimulatedActionExecutor` clears connection pools, resets synthetic error rates, and sets the service environment back to `HEALTHY`.

### Step 6: Verify Audit Trail & Governance
1. Log in as **`admin@forgeops.local`** (`Password123!`).
2. Navigate to **Audit Trail** (`/audit`).
3. Inspect the tamper-evident log containing timestamped records for authentication, incident creation, approval review comments, and execution results.

---

## 8. License

Distributed under the Apache 2.0 License.
