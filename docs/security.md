# ForgeOps Security Specification

## 1. Authentication Architecture

ForgeOps uses JSON Web Tokens (JWT) for stateless, scalable REST API authentication.

### Token Design & Life Cycle
- **Algorithm**: HMAC-SHA256 (`HS256`) signed with a minimum 256-bit cryptographically secure key loaded from the environment (`JWT_SECRET`).
- **Access Token Expiration**: Configurable (default 24 hours for local development, 15-60 minutes in production).
- **Claims**:
  - `sub`: User UUID
  - `email`: User email address
  - `roles`: Array of role names (e.g. `["ROLE_ENGINEER"]`)
  - `iat`: Issued At timestamp
  - `exp`: Expiration timestamp
- **Transport**: Transmitted via standard `Authorization: Bearer <token>` HTTP header.
- **Frontend Storage Consideration**:
  - In the local development React SPA, tokens are maintained in in-memory React context with optional session-storage caching.
  - Documented production hardening: Secure, HttpOnly, SameSite cookies to mitigate cross-site scripting (XSS) token exfiltration.
  - No credentials, master keys, or passwords ever touch browser storage.

---

## 2. Password Storage & Hashing
- Passwords are encrypted using Spring Security's `BCryptPasswordEncoder` with strength/work factor of 12.
- Plaintext passwords are never persisted, never logged, and never included in API responses or serialized JSON error payloads.

---

## 3. Role-Based Access Control (RBAC) Matrix

ForgeOps implements least privilege across four core operational roles:

| API Domain | Endpoint / Action | VIEWER | ENGINEER | APPROVER | ADMIN |
|---|---|:---:|:---:|:---:|:---:|
| **Auth** | Login / Logout / Me |  |  |  |  |
| **Services** | View Services & Metrics |  |  |  |  |
| **Services** | Create / Edit Services | ❌ |  | ❌ |  |
| **Dashboard** | View Dashboard & Health |  |  |  |  |
| **Incidents** | View Incidents & Notes |  |  |  |  |
| **Incidents** | Create / Edit / Assign Incidents | ❌ |  | ❌ |  |
| **Incidents** | Add Investigation Notes | ❌ |  | ❌ |  |
| **Incidents** | Change Status / Resolve Incident | ❌ |  | ❌ |  |
| **AI Assistant** | Trigger Local AI Incident Analysis | ❌ |  | ❌ |  |
| **Action Requests** | View Action Requests |  |  |  |  |
| **Action Requests** | Create Action Request | ❌ |  | ❌ |  |
| **Action Requests** | Approve / Reject Action Request | ❌ | ❌ | *(Except own)* |  |
| **Action Requests** | Execute Approved Simulated Action | ❌ |  | ❌ |  |
| **Audit Log** | View Audit Events & Timeline | ❌ | ❌ |  |  |
| **Admin** | User & Role Management | ❌ | ❌ | ❌ |  |

*Note: Authorization is enforced at the Spring Security filter and controller level via `@PreAuthorize("hasRole('...')")` and in service methods. The UI also adapts dynamically by hiding or disabling unauthorized action buttons, but never relies solely on frontend gating.*

---

## 4. Operational Invariants & Segregation of Duties

1. **Self-Approval Prevention (Dual-Control Invariant)**:
   - An engineer who creates an action request cannot approve or reject that same request.
   - If an Approver or Admin creates an action request, the backend rejects any attempt by that same user to approve it:
     ```java
     if (actionRequest.getRequestedBy().getId().equals(currentUser.getId())) {
         throw new BusinessRuleException("Requester cannot approve their own action request.");
     }
     ```
2. **State Gate Before Execution**:
   - Only requests with `approvalStatus == APPROVED` can enter execution.
   - Any attempt to execute a `PENDING_APPROVAL`, `REJECTED`, or already `EXECUTED` action returns HTTP 409 Conflict.
3. **Action Execution Idempotency**:
   - Each action request contains an `idempotencyKey` and atomic status update to prevent race conditions or repeated execution.

---

## 5. Network, CORS, and Headers

- **CORS Configuration**: Restricts access explicitly to the trusted frontend development origin (`http://localhost:5173` by default) via `CorsConfigurationSource`. Wildcard origins (`*`) with credentials are strictly forbidden.
- **Security Headers**:
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: DENY`
  - `Content-Security-Policy`: Default restrictive policy preventing inline eval and external asset hijacking.
  - `Strict-Transport-Security`: HSTS enabled for production configurations.

---

## 6. Audit & Logging Sanitization

- Sensitive fields (`password`, `token`, `authorization`, `secret`) are scrubbed before reaching log appenders.
- Correlation IDs (`X-Correlation-Id` or MDC `correlationId`) are injected on incoming HTTP requests and propagated to structured logs to trace transactions across layers without logging user secrets.
