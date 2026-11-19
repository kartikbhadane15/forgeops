package com.forgeops.config;

import com.forgeops.metrics.MetricSampleEntity;
import com.forgeops.metrics.MetricSampleRepository;
import com.forgeops.metrics.MetricType;
import com.forgeops.serviceinventory.*;
import com.forgeops.user.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@Profile("!prod")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;
    private final ServiceRepository serviceRepo;
    private final EnvironmentRepository envRepo;
    private final MetricSampleRepository metricRepo;

    public DataInitializer(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder encoder,
                           ServiceRepository serviceRepo, EnvironmentRepository envRepo,
                           MetricSampleRepository metricRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
        this.serviceRepo = serviceRepo;
        this.envRepo = envRepo;
        this.metricRepo = metricRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Running DataInitializer...");
        seedRolesAndUsers();
        seedServicesAndMetrics();
        log.info("DataInitializer complete.");
    }

    private void seedRolesAndUsers() {
        RoleEntity viewer   = getOrCreateRole(RoleName.ROLE_VIEWER, "Read-only platform viewer");
        RoleEntity engineer = getOrCreateRole(RoleName.ROLE_ENGINEER, "Operations engineer");
        RoleEntity approver = getOrCreateRole(RoleName.ROLE_APPROVER, "Action approver");
        RoleEntity admin    = getOrCreateRole(RoleName.ROLE_ADMIN, "Platform administrator");

        String hash = encoder.encode("Password123!");
        seedUser("viewer@forgeops.local",   hash, "Valerie Viewer",  Set.of(viewer));
        seedUser("engineer@forgeops.local", hash, "Devon Engineer",  Set.of(engineer));
        seedUser("approver@forgeops.local", hash, "Alex Approver",   Set.of(approver));
        seedUser("admin@forgeops.local",    hash, "Sam Admin",        Set.of(admin));
    }

    private void seedServicesAndMetrics() {
        record SvcDef(String key, String name, String team, Criticality crit) {}

        List<SvcDef> defs = List.of(
            new SvcDef("orders-api",          "Orders API",            "Commerce Platform",  Criticality.CRITICAL),
            new SvcDef("identity-service",    "Identity Service",      "Platform Security",  Criticality.CRITICAL),
            new SvcDef("notification-worker", "Notification Worker",   "Comms Platform",     Criticality.MEDIUM),
            new SvcDef("billing-api",         "Billing API",           "Billing Platform",   Criticality.HIGH),
            new SvcDef("inventory-service",   "Inventory Service",     "Commerce Platform",  Criticality.HIGH)
        );

        for (SvcDef def : defs) {
            if (serviceRepo.existsByServiceKey(def.key())) continue;

            ServiceEntity svc = new ServiceEntity();
            svc.setServiceKey(def.key());
            svc.setName(def.name());
            svc.setDescription("Simulated service for ForgeOps portfolio demonstration.");
            svc.setOwningTeam(def.team());
            svc.setCriticality(def.crit());
            svc.setLifecycleStatus(LifecycleStatus.ACTIVE);
            serviceRepo.save(svc);

            EnvironmentEntity prod = buildEnv(svc, "Production", "PRODUCTION", "us-east-1 (simulated)", EnvironmentStatus.HEALTHY);
            EnvironmentEntity staging = buildEnv(svc, "Staging", "STAGING", "us-west-2 (simulated)", EnvironmentStatus.HEALTHY);
            envRepo.saveAll(List.of(prod, staging));

            seedMetrics(svc, prod);
            log.info("Seeded service: {}", def.key());
        }

        // Make orders-api production environment look degraded with high latency
        serviceRepo.findByServiceKey("orders-api").ifPresent(svc -> {
            envRepo.findByServiceIdAndName(svc.getId(), "Production").ifPresent(env -> {
                env.setStatus(EnvironmentStatus.DEGRADED);
                envRepo.save(env);
            });
        });
    }

    private EnvironmentEntity buildEnv(ServiceEntity svc, String name, String type, String region, EnvironmentStatus status) {
        EnvironmentEntity env = new EnvironmentEntity();
        env.setService(svc);
        env.setName(name);
        env.setEnvType(type);
        env.setRegionLabel(region);
        env.setStatus(status);
        return env;
    }

    private void seedMetrics(ServiceEntity svc, EnvironmentEntity env) {
        boolean isOrders = svc.getServiceKey().equals("orders-api");
        Random rand = new Random(svc.getServiceKey().hashCode());
        List<MetricSampleEntity> samples = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            Instant ts = Instant.now().minus(i * 5L, ChronoUnit.MINUTES);
            double latency = isOrders ? 650 + rand.nextDouble() * 400 : 80 + rand.nextDouble() * 60;
            double errRate = isOrders ? 2.5 + rand.nextDouble() * 3.0 : rand.nextDouble() * 0.3;
            double cpu     = 20 + rand.nextDouble() * 40;
            double mem     = 40 + rand.nextDouble() * 30;

            samples.add(metric(svc, env, MetricType.LATENCY_P95,       latency, "ms",   ts));
            samples.add(metric(svc, env, MetricType.ERROR_RATE,         errRate, "%",    ts));
            samples.add(metric(svc, env, MetricType.CPU_UTILIZATION,    cpu,     "%",    ts));
            samples.add(metric(svc, env, MetricType.MEMORY_UTILIZATION, mem,     "%",    ts));
        }
        metricRepo.saveAll(samples);
    }

    private MetricSampleEntity metric(ServiceEntity svc, EnvironmentEntity env,
                                      MetricType type, double value, String unit, Instant ts) {
        MetricSampleEntity m = new MetricSampleEntity(svc, env, type, value, unit);
        m.setSampledAt(ts);
        return m;
    }

    private RoleEntity getOrCreateRole(RoleName name, String desc) {
        return roleRepo.findByName(name).orElseGet(() -> roleRepo.save(new RoleEntity(name, desc)));
    }

    private void seedUser(String email, String hash, String name, Set<RoleEntity> roles) {
        userRepo.findByEmail(email).ifPresentOrElse(u -> {
            u.setPasswordHash(hash);
            u.setEnabled(true);
            u.setRoles(roles);
            userRepo.save(u);
            log.info("Updated existing user credentials: {}", email);
        }, () -> {
            UserEntity u = new UserEntity(email, hash, name);
            u.setRoles(roles);
            userRepo.save(u);
            log.info("Seeded user: {}", email);
        });
    }
}
