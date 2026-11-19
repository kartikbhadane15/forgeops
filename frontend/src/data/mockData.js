export const MOCK_SUMMARY = {
  totalServices: 5,
  healthyServices: 4,
  degradedServices: 1,
  unavailableServices: 0,
  openIncidents: 2,
  sev1Count: 0,
  sev2Count: 1,
  sev3Count: 1,
  sev4Count: 0,
  pendingApprovalCount: 1
};

export const MOCK_SERVICES = [
  {
    id: "s1111111-1111-1111-1111-111111111111",
    serviceKey: "orders-api",
    name: "Orders API",
    owningTeam: "Commerce Platform",
    criticality: "CRITICAL",
    lifecycleStatus: "ACTIVE",
    productionHealth: "DEGRADED",
    latencyP95Ms: 1240,
    errorRatePct: 4.85,
    environments: [
      { id: "e11", name: "Production", type: "PRODUCTION", status: "DEGRADED" },
      { id: "e12", name: "Staging", type: "STAGING", status: "HEALTHY" }
    ]
  },
  {
    id: "s2222222-2222-2222-2222-222222222222",
    serviceKey: "identity-service",
    name: "Identity & Auth Service",
    owningTeam: "Platform Security",
    criticality: "CRITICAL",
    lifecycleStatus: "ACTIVE",
    productionHealth: "HEALTHY",
    latencyP95Ms: 82,
    errorRatePct: 0.04,
    environments: [
      { id: "e21", name: "Production", type: "PRODUCTION", status: "HEALTHY" }
    ]
  },
  {
    id: "s3333333-3333-3333-3333-333333333333",
    serviceKey: "billing-api",
    name: "Billing & Invoicing API",
    owningTeam: "Billing Platform",
    criticality: "HIGH",
    lifecycleStatus: "ACTIVE",
    productionHealth: "HEALTHY",
    latencyP95Ms: 145,
    errorRatePct: 0.12,
    environments: [
      { id: "e31", name: "Production", type: "PRODUCTION", status: "HEALTHY" }
    ]
  },
  {
    id: "s4444444-4444-4444-4444-444444444444",
    serviceKey: "notification-worker",
    name: "Notification Worker",
    owningTeam: "Comms Platform",
    criticality: "MEDIUM",
    lifecycleStatus: "ACTIVE",
    productionHealth: "HEALTHY",
    latencyP95Ms: 95,
    errorRatePct: 0.0,
    environments: [
      { id: "e41", name: "Production", type: "PRODUCTION", status: "HEALTHY" }
    ]
  },
  {
    id: "s5555555-5555-5555-5555-555555555555",
    serviceKey: "inventory-service",
    name: "Inventory Service",
    owningTeam: "Commerce Platform",
    criticality: "HIGH",
    lifecycleStatus: "ACTIVE",
    productionHealth: "HEALTHY",
    latencyP95Ms: 110,
    errorRatePct: 0.08,
    environments: [
      { id: "e51", name: "Production", type: "PRODUCTION", status: "HEALTHY" }
    ]
  }
];

export const MOCK_INCIDENTS = [
  {
    id: "inc-0001-aaaa-bbbb",
    incidentNumber: "INC-2026-0001",
    title: "High P95 latency and thread exhaustion on checkout flow",
    description: "Orders API production instances reporting P95 response times exceeding 1200ms. Telemetry indicates connection pool saturation and downstream database query queues.",
    serviceId: "s1111111-1111-1111-1111-111111111111",
    serviceName: "Orders API",
    environmentId: "e11",
    environmentName: "Production",
    severity: "SEV2",
    status: "INVESTIGATING",
    assignee: { fullName: "Devon Engineer", email: "engineer@forgeops.local" },
    createdBy: { fullName: "Sam Admin", email: "admin@forgeops.local" },
    createdAt: new Date(Date.now() - 35 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 5 * 60 * 1000).toISOString(),
    notes: [
      {
        id: "n1",
        authorName: "Sam Admin",
        noteBody: "Automated alert fired from P95 latency threshold check (>800ms for 3 consecutive intervals).",
        createdAt: new Date(Date.now() - 30 * 60 * 1000).toISOString()
      },
      {
        id: "n2",
        authorName: "Devon Engineer",
        noteBody: "Assigned to Devon. Investigating thread dump and HikariCP pool status. Database CPU is normal at 32%.",
        createdAt: new Date(Date.now() - 15 * 60 * 1000).toISOString()
      }
    ]
  },
  {
    id: "inc-0002-cccc-dddd",
    incidentNumber: "INC-2026-0002",
    title: "Webhook delivery failure rate spike on partner callbacks",
    description: "Notification worker reporting transient 502 responses when dispatching billing confirmation webhooks to third-party endpoints.",
    serviceId: "s4444444-4444-4444-4444-444444444444",
    serviceName: "Notification Worker",
    environmentId: "e41",
    environmentName: "Production",
    severity: "SEV3",
    status: "OPEN",
    assignee: null,
    createdBy: { fullName: "Valerie Viewer", email: "viewer@forgeops.local" },
    createdAt: new Date(Date.now() - 85 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 70 * 60 * 1000).toISOString(),
    notes: []
  }
];

export const MOCK_APPROVALS = [
  {
    id: "req-0001-xxxx-yyyy",
    idempotencyKey: "req-orders-api-restart-2026",
    incidentId: "inc-0001-aaaa-bbbb",
    incidentNumber: "INC-2026-0001",
    incidentTitle: "High P95 latency and thread exhaustion on checkout flow",
    targetServiceId: "s1111111-1111-1111-1111-111111111111",
    targetServiceName: "Orders API",
    actionType: "RESTART_DEMO_SERVICE",
    reason: "Flush starved database connection pools and reset clogged worker threads to restore nominal latency under 150ms.",
    riskLevel: "MEDIUM",
    approvalStatus: "PENDING_APPROVAL",
    requestedBy: { id: "f2222222-2222-2222-2222-222222222222", fullName: "Devon Engineer", email: "engineer@forgeops.local" },
    reviewedBy: null,
    reviewComment: null,
    executedBy: null,
    executionResult: null,
    requestedAt: new Date(Date.now() - 10 * 60 * 1000).toISOString(),
    reviewedAt: null,
    executedAt: null
  }
];

export const MOCK_AUDIT_EVENTS = [
  {
    id: "aud-001",
    actor: { fullName: "Devon Engineer", email: "engineer@forgeops.local" },
    eventType: "ACTION_REQUEST_CREATED",
    entityType: "ACTION_REQUEST",
    entityId: "req-0001-xxxx-yyyy",
    correlationId: "req-orders-api-restart-2026",
    safeSummary: "Action request 'RESTART_DEMO_SERVICE' created for service 'Orders API'",
    detailsJson: JSON.stringify({ actionType: "RESTART_DEMO_SERVICE", riskLevel: "MEDIUM" }),
    createdAt: new Date(Date.now() - 10 * 60 * 1000).toISOString()
  },
  {
    id: "aud-002",
    actor: { fullName: "Devon Engineer", email: "engineer@forgeops.local" },
    eventType: "INCIDENT_STATUS_CHANGED",
    entityType: "INCIDENT",
    entityId: "inc-0001-aaaa-bbbb",
    correlationId: "INC-2026-0001",
    safeSummary: "Incident INC-2026-0001 status transitioned from OPEN to INVESTIGATING",
    detailsJson: JSON.stringify({ previousStatus: "OPEN", targetStatus: "INVESTIGATING" }),
    createdAt: new Date(Date.now() - 15 * 60 * 1000).toISOString()
  },
  {
    id: "aud-003",
    actor: { fullName: "Sam Admin", email: "admin@forgeops.local" },
    eventType: "INCIDENT_CREATED",
    entityType: "INCIDENT",
    entityId: "inc-0001-aaaa-bbbb",
    correlationId: "INC-2026-0001",
    safeSummary: "Incident INC-2026-0001 created for Orders API",
    detailsJson: JSON.stringify({ severity: "SEV2", service: "Orders API", environment: "Production" }),
    createdAt: new Date(Date.now() - 35 * 60 * 1000).toISOString()
  },
  {
    id: "aud-004",
    actor: { fullName: "Devon Engineer", email: "engineer@forgeops.local" },
    eventType: "AUTH_LOGIN_SUCCESS",
    entityType: "USER",
    entityId: "f2222222-2222-2222-2222-222222222222",
    correlationId: null,
    safeSummary: "User engineer@forgeops.local successfully authenticated via JWT",
    detailsJson: JSON.stringify({ email: "engineer@forgeops.local" }),
    createdAt: new Date(Date.now() - 40 * 60 * 1000).toISOString()
  }
];
