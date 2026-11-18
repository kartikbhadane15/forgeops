# ForgeOps AI Safety & Deterministic Analysis Design

## 1. Safety Principles for Enterprise Operations

In mission-critical operational environments, autonomous AI systems must never be granted direct execution authority over infrastructure, databases, or services. ForgeOps enforces three immutable safety pillars:

1. **Zero External Dependence (Air-Gapped Reliability)**:
   - The analysis assistant operates completely offline within the local Spring Boot application.
   - It makes zero calls to external LLMs, hosted cloud APIs, or third-party web services.
   - Requires zero API keys or external subscriptions.
   - Analysis results are deterministic, reproducible, and immune to upstream provider outages, rate limits, or latency spikes.
2. **Untrusted Recommendation Principle**:
   - Every generated recommendation is categorized as **unverified advisory guidance**.
   - Analysis output is never automatically promoted to an executable state without an explicit human submission.
3. **Mandatory Human-in-the-Loop & Segregation of Duties**:
   - An analysis cannot execute an action directly.
   - A human engineer must explicitly select a recommended action and create an `ActionRequest`.
   - A distinct authorized approver must inspect the justification, risk rating, and target service before approving.
   - Actions are simulated and restricted to a strict backend whitelist.

```mermaid
sequenceDiagram
    autonumber
    actor Engineer as Engineer
    participant App as Spring Boot REST API
    participant AI as Rule-Based AI Engine
    participant DB as PostgreSQL DB
    actor Approver as Approver
    participant Executor as Safe Action Executor

    Engineer->>App: POST /api/v1/incidents/{id}/ai-analysis
    App->>DB: Fetch Incident, Service, & Metric History
    App->>AI: Evaluate Context (Deterministic Heuristics)
    AI-->>App: Return Structured Analysis DTO
    App->>DB: Persist AIAnalysis Record
    App-->>Engineer: Render Analysis (Summary, Causes, Steps, Proposed Actions)

    Note over Engineer,App: Proposal is NOT executable yet

    Engineer->>App: POST /api/v1/action-requests (idempotencyKey, actionType, reason)
    App->>DB: Persist ActionRequest (Status: PENDING_APPROVAL)
    App-->>Engineer: Confirmation (Request Pending)

    Note over Approver,App: Segregation of Duties (Approver != Requester)

    Approver->>App: POST /api/v1/action-requests/{id}/approve
    App->>DB: Validate Approver != Requester; Update Status: APPROVED
    App->>DB: Record Audit Event (ACTION_APPROVED)

    Engineer->>App: POST /api/v1/action-requests/{id}/execute
    App->>App: Verify Status == APPROVED & Idempotency
    App->>Executor: Execute Simulated Safe Action
    Executor-->>App: Simulated Execution Result
    App->>DB: Update Status: EXECUTED & Record Result
    App->>DB: Record Audit Event (ACTION_EXECUTED)
    App-->>Engineer: Return Execution Outcome
```

---

## 2. Deterministic Rule-Based Engine Architecture

The analysis assistant evaluates incident context using deterministic heuristic rules grounded in operational observability patterns:

### Rule 1: High Latency & Saturated Downstream
- **Trigger**: P95 latency > 800ms, CPU normal (<70%), Downstream dependencies reporting errors.
- **Inference**: Downstream bottleneck or connection pool starvation.
- **Proposed Action**: `RESTART_DEMO_SERVICE` (Medium Risk) or `MARK_SERVICE_MAINTENANCE` (Low Risk).

### Rule 2: Memory Leak / Saturated Heap
- **Trigger**: Memory utilization > 88% continuously over recent samples.
- **Inference**: Resource exhaustion or memory leak.
- **Proposed Action**: `RESTART_DEMO_SERVICE` (Medium Risk).

### Rule 3: Error Rate Spike with Stable Resource Utilization
- **Trigger**: Error rate > 5.0%, CPU & Memory within normal limits.
- **Inference**: Bad deployment, invalid downstream contract, or persistent transient alert.
- **Proposed Action**: `CLEAR_DEMO_ALERT` (Low Risk) or `MARK_SERVICE_MAINTENANCE` (Low Risk).

### Rule 4: Critical Service Down / Unavailable
- **Trigger**: Service status `UNAVAILABLE` or Health Check failing.
- **Inference**: Total process failure.
- **Proposed Action**: `RESTART_DEMO_SERVICE` (High Risk) and `MARK_SERVICE_MAINTENANCE`.

If metric samples or context are missing or contradictory, the engine gracefully falls back to identifying data deficiencies in the `limitations` section and refuses to recommend high-risk automated actions.

---

## 3. Strict Server-Side Action Whitelist

Under no circumstances can the AI engine or user propose arbitrary shell commands, SQL queries, HTTP endpoints, or operating system routines. Only the following discrete enum actions are recognized by the system:

| Action Enum | Allowed Target | Simulated Operation Performed | Risk Level |
|---|---|---|:---:|
| `MARK_SERVICE_MAINTENANCE` | Valid registered service | Sets service lifecycle status to `MAINTENANCE` and updates environment status | LOW |
| `CLEAR_DEMO_ALERT` | Valid registered service | Resets synthetic alert triggers and simulated error rate to 0% | LOW |
| `RESTART_DEMO_SERVICE` | Valid registered service | Simulates a rolling service restart, clears synthetic memory/connection pools, returns status to `ACTIVE` | MEDIUM |

Any action outside this enum is rejected with `VALIDATION_ERROR` (HTTP 400).

---

## 4. Idempotency & Replay Protection

- Each action request requires a unique `idempotencyKey` provided by the client or generated upon proposal conversion.
- The `execute` transition uses optimistic concurrency control (`@Version` or database-level row lock `SELECT FOR UPDATE`) to guarantee that simultaneous execution triggers cannot run twice.
- If an action has already reached `EXECUTED`, subsequent calls return the existing execution result without re-triggering the executor.
