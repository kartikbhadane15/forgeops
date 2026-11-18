# ForgeOps REST API Contract Specification

All endpoints are prefixed with `/api/v1`. All timestamps are ISO 8601 UTC strings (e.g. `2026-11-19T03:00:00Z`).

## 1. Standard Envelopes & Error Handling

### 1.1 Success Envelope (Single Entity)
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful",
  "timestamp": "2026-11-19T03:00:00Z"
}
```

### 1.2 Paged Success Envelope
```json
{
  "success": true,
  "data": [ ... ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 105,
    "totalPages": 6,
    "first": true,
    "last": false
  },
  "timestamp": "2026-11-19T03:00:00Z"
}
```

### 1.3 Standard Error Envelope
```json
{
  "timestamp": "2026-11-19T03:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "The request contains invalid fields.",
  "path": "/api/v1/incidents",
  "fieldErrors": {
    "title": "Title must not be blank.",
    "severity": "Severity must be one of: SEV1, SEV2, SEV3, SEV4"
  }
}
```

Common Error Codes:
- `VALIDATION_ERROR` (400)
- `UNAUTHORIZED` (401)
- `FORBIDDEN` (403)
- `NOT_FOUND` (404)
- `CONFLICT` / `INVALID_STATE` (409)
- `INTERNAL_ERROR` (500)

---

## 2. Authentication & Current User

### `POST /api/v1/auth/login`
- **Auth**: Public
- **Request Body**:
  ```json
  {
    "email": "engineer@forgeops.local",
    "password": "Password123!"
  }
  ```
- **Response** (`200 OK`):
  ```json
  {
    "token": "eyJhbGciOi...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": "e4b2d3aa-9b1d-4001-a1b2-c3d4e5f6a7b8",
      "email": "engineer@forgeops.local",
      "fullName": "Devon Engineer",
      "roles": ["ROLE_ENGINEER"]
    }
  }
  ```

### `POST /api/v1/auth/logout`
- **Auth**: Authenticated (Any role)
- **Response** (`200 OK`):
  ```json
  { "message": "Successfully logged out." }
  ```

### `GET /api/v1/auth/me`
- **Auth**: Authenticated (Any role)
- **Response** (`200 OK`): Current user profile and authorities.

---

## 3. User Administration (Admin Only)

### `GET /api/v1/admin/users`
- **Auth**: `ROLE_ADMIN`
- **Query Params**: `page`, `size`, `search`
- **Response** (`200 OK`): Paged list of user accounts with roles and status.

### `POST /api/v1/admin/users`
- **Auth**: `ROLE_ADMIN`
- **Request Body**:
  ```json
  {
    "email": "newuser@forgeops.local",
    "fullName": "Jane Doe",
    "password": "TemporaryPassword123!",
    "roles": ["ROLE_VIEWER"]
  }
  ```
- **Response** (`201 Created`): Created user entity.

### `PATCH /api/v1/admin/users/{id}/roles`
- **Auth**: `ROLE_ADMIN`
- **Request Body**: `{ "roles": ["ROLE_ENGINEER", "ROLE_APPROVER"] }`

### `PATCH /api/v1/admin/users/{id}/status`
- **Auth**: `ROLE_ADMIN`
- **Request Body**: `{ "enabled": false }`

---

## 4. Services & Environments

### `GET /api/v1/services`
- **Auth**: Any authenticated user (`VIEWER`, `ENGINEER`, `APPROVER`, `ADMIN`)
- **Query Params**: `search`, `criticality`, `lifecycleStatus`, `page`, `size`, `sort`
- **Response** (`200 OK`): Paged list of services.

### `POST /api/v1/services`
- **Auth**: `ROLE_ENGINEER`, `ROLE_ADMIN`
- **Request Body**:
  ```json
  {
    "serviceKey": "payments-worker",
    "name": "Payments Background Worker",
    "description": "Processes asynchronous payment webhooks and settlement events.",
    "owningTeam": "Billing Platform",
    "criticality": "HIGH",
    "lifecycleStatus": "ACTIVE"
  }
  ```
- **Response** (`201 Created`)

### `GET /api/v1/services/{id}`
- **Auth**: Any authenticated user
- **Response** (`200 OK`): Service details with environment summaries.

### `PUT /api/v1/services/{id}`
- **Auth**: `ROLE_ENGINEER`, `ROLE_ADMIN`
- **Response** (`200 OK`): Updated service details.

### `GET /api/v1/services/{id}/environments`
- **Auth**: Any authenticated user
- **Response** (`200 OK`): List of environments for the service.

### `GET /api/v1/services/{id}/incidents`
- **Auth**: Any authenticated user
- **Response** (`200 OK`): Recent incidents for this service.

### `GET /api/v1/services/{id}/metrics`
- **Auth**: Any authenticated user
- **Query Params**: `timeRange` (`1h`, `6h`, `24h`), `metricType`
- **Response** (`200 OK`): Time series metric samples for charting.

---

## 5. Dashboard Summary

### `GET /api/v1/dashboard/summary`
- **Auth**: Any authenticated user
- **Response** (`200 OK`):
  ```json
  {
    "totalServices": 12,
    "healthyServices": 9,
    "degradedServices": 2,
    "unavailableServices": 1,
    "openIncidents": {
      "total": 4,
      "sev1": 1,
      "sev2": 1,
      "sev3": 1,
      "sev4": 1
    },
    "pendingApprovalCount": 2
  }
  ```

### `GET /api/v1/dashboard/service-health`
- **Auth**: Any authenticated user
- **Response** (`200 OK`): List of all services with computed health status, latest p95 latency, error rate, and active incidents.

### `GET /api/v1/dashboard/recent-incidents`
- **Auth**: Any authenticated user
- **Response** (`200 OK`): Top 5 recent incidents.

---

## 6. Incident Management

### `GET /api/v1/incidents`
- **Auth**: Any authenticated user
- **Query Params**: `status`, `severity`, `serviceId`, `assigneeId`, `page`, `size`
- **Response** (`200 OK`): Paged incident list.

### `POST /api/v1/incidents`
- **Auth**: `ROLE_ENGINEER`, `ROLE_ADMIN`
- **Request Body**:
  ```json
  {
    "title": "Elevated P95 Latency on Orders API",
    "description": "P95 response latency exceeded 800ms threshold in Production environment.",
    "serviceId": "1b9d6bcd-bbfd-4b2d-9b5d-ab8dfbbd4bed",
    "environmentId": "2c9d6bcd-bbfd-4b2d-9b5d-ab8dfbbd4bed",
    "severity": "SEV2",
    "assigneeId": "e4b2d3aa-9b1d-4001-a1b2-c3d4e5f6a7b8"
  }
  ```
- **Response** (`201 Created`): Returns created incident with generated `incidentNumber` (e.g. `INC-2026-0001`).

### `GET /api/v1/incidents/{id}`
- **Auth**: Any authenticated user
- **Response** (`200 OK`): Incident details, investigation notes, and timeline.

### `PATCH /api/v1/incidents/{id}`
- **Auth**: `ROLE_ENGINEER`, `ROLE_ADMIN`
- **Request Body**: Update title, description, or severity.

### `POST /api/v1/incidents/{id}/assign`
- **Auth**: `ROLE_ENGINEER`, `ROLE_ADMIN`
- **Request Body**: `{ "assigneeId": "..." }`

### `POST /api/v1/incidents/{id}/notes`
- **Auth**: `ROLE_ENGINEER`, `ROLE_ADMIN`
- **Request Body**: `{ "noteBody": "Observed connection pool saturation on database replica." }`
- **Response** (`201 Created`)

### `POST /api/v1/incidents/{id}/transitions`
- **Auth**: `ROLE_ENGINEER`, `ROLE_ADMIN`
- **Request Body**:
  ```json
  {
    "targetStatus": "RESOLVED",
    "resolutionSummary": "Service restarted; connections returned to normal levels."
  }
  ```
- **State Transition Invariants**:
  - `OPEN` -> `INVESTIGATING`, `MITIGATED`, `RESOLVED`
  - `INVESTIGATING` -> `MITIGATED`, `RESOLVED`
  - `MITIGATED` -> `RESOLVED`, `INVESTIGATING`
  - `RESOLVED` cannot be transitioned unless reopened by Admin.
  - Moving to `RESOLVED` strictly requires `resolutionSummary`.

---

## 7. AI Incident Assistant & Action Approvals

### `POST /api/v1/incidents/{id}/ai-analysis`
- **Auth**: `ROLE_ENGINEER`, `ROLE_ADMIN`
- **Request Body**: `{}` (Analyzes current incident and recent metrics)
- **Response** (`200 OK`):
  ```json
  {
    "id": "7f8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
    "incidentId": "3b9d6bcd-bbfd-4b2d-9b5d-ab8dfbbd4bed",
    "summary": "Orders API in Production is experiencing elevated P95 latency (920ms) and 4.2% error rate.",
    "possibleCauses": [
      "Thread pool exhaustion due to slow downstream billing dependency",
      "Memory leak or high GC pause duration",
      "Database connection pool saturation"
    ],
    "investigationSteps": [
      "Review JVM heap and garbage collection metrics over the last 30 minutes",
      "Inspect connection pool active vs idle connection graphs",
      "Verify health status of downstream billing-api"
    ],
    "proposedActions": [
      {
        "actionType": "RESTART_DEMO_SERVICE",
        "targetServiceKey": "orders-api",
        "reason": "Controlled rolling restart clears saturated in-flight worker threads and restores baseline responsiveness.",
        "riskLevel": "MEDIUM"
      }
    ],
    "risk": "MEDIUM",
    "limitations": [
      "Analysis derived from local deterministic heuristic rules and synthetic telemetry samples.",
      "Root cause requires engineer validation of database logs."
    ],
    "createdAt": "2026-11-19T03:15:00Z"
  }
  ```

### `GET /api/v1/incidents/{id}/ai-analyses`
- **Auth**: Any authenticated user
- **Response** (`200 OK`): History of generated AI analyses for this incident.

### `POST /api/v1/action-requests`
- **Auth**: `ROLE_ENGINEER`, `ROLE_ADMIN`
- **Request Body**:
  ```json
  {
    "incidentId": "3b9d6bcd-bbfd-4b2d-9b5d-ab8dfbbd4bed",
    "targetServiceKey": "orders-api",
    "actionType": "RESTART_DEMO_SERVICE",
    "reason": "Controlled restart to clear saturated thread pool as proposed in analysis.",
    "riskLevel": "MEDIUM",
    "idempotencyKey": "req-orders-api-restart-20261119-01"
  }
  ```
- **Response** (`201 Created`): Created request with status `PENDING_APPROVAL`.

### `GET /api/v1/action-requests`
- **Auth**: Any authenticated user
- **Query Params**: `status` (`PENDING_APPROVAL`, `APPROVED`, `REJECTED`, `EXECUTED`, `FAILED`), `serviceKey`, `page`, `size`
- **Response** (`200 OK`): Paged list of action requests.

### `GET /api/v1/action-requests/{id}`
- **Auth**: Any authenticated user
- **Response** (`200 OK`): Action request details.

### `POST /api/v1/action-requests/{id}/approve`
- **Auth**: `ROLE_APPROVER`, `ROLE_ADMIN`
- **Invariant**: **Requester cannot approve their own action request!**
- **Request Body**:
  ```json
  {
    "comment": "Approved following review of staging behavior and customer impact window."
  }
  ```
- **Response** (`200 OK`): Request marked as `APPROVED`.

### `POST /api/v1/action-requests/{id}/reject`
- **Auth**: `ROLE_APPROVER`, `ROLE_ADMIN`
- **Request Body**:
  ```json
  {
    "comment": "Peak traffic window; postpone restart until off-peak hours."
  }
  ```
- **Response** (`200 OK`): Request marked as `REJECTED`. Rejection comment is mandatory.

### `POST /api/v1/action-requests/{id}/execute`
- **Auth**: `ROLE_ENGINEER`, `ROLE_ADMIN`
- **Request Headers**: `X-Idempotency-Key` (Optional, validated against action request)
- **Invariants**:
  - Request MUST be in `APPROVED` status.
  - Cannot execute a `PENDING_APPROVAL`, `REJECTED`, or already `EXECUTED` request.
  - Atomic transition to `EXECUTED` prevents race conditions and double execution.
- **Response** (`200 OK`):
  ```json
  {
    "requestId": "5d9d6bcd-bbfd-4b2d-9b5d-ab8dfbbd4bed",
    "status": "EXECUTED",
    "executionResult": "Simulated restart completed successfully in 320ms. Service returned to HEALTHY state.",
    "executedAt": "2026-11-19T03:20:00Z"
  }
  ```

---

## 8. Audit Trail

### `GET /api/v1/audit-events`
- **Auth**: `ROLE_APPROVER`, `ROLE_ADMIN`
- **Query Params**: `eventType`, `entityType`, `actorUserId`, `startDate`, `endDate`, `page`, `size`
- **Response** (`200 OK`): Append-only audit logs.

### `GET /api/v1/audit-events/{id}`
- **Auth**: `ROLE_APPROVER`, `ROLE_ADMIN`
- **Response** (`200 OK`): Detailed event payload.
