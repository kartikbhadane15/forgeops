package com.forgeops.config;

import com.forgeops.user.RoleEntity;
import com.forgeops.user.RoleName;
import com.forgeops.user.RoleRepository;
import com.forgeops.user.UserEntity;
import com.forgeops.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@Profile("!prod")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing baseline roles and demo users...");

        // 1. Initialize Roles
        RoleEntity viewerRole = getOrCreateRole(RoleName.ROLE_VIEWER, "Read-only viewer of dashboards and incidents");
        RoleEntity engineerRole = getOrCreateRole(RoleName.ROLE_ENGINEER, "Operations engineer able to investigate and propose actions");
        RoleEntity approverRole = getOrCreateRole(RoleName.ROLE_APPROVER, "Authorized operations approver for action requests");
        RoleEntity adminRole = getOrCreateRole(RoleName.ROLE_ADMIN, "Platform administrator with full user and service management");

        // 2. Initialize Seed Users (Password: Password123!)
        String defaultPasswordHash = passwordEncoder.encode("Password123!");

        getOrCreateUser("viewer@forgeops.local", defaultPasswordHash, "Valerie Viewer", Set.of(viewerRole));
        getOrCreateUser("engineer@forgeops.local", defaultPasswordHash, "Devon Engineer", Set.of(engineerRole));
        getOrCreateUser("approver@forgeops.local", defaultPasswordHash, "Alex Approver", Set.of(approverRole));
        getOrCreateUser("admin@forgeops.local", defaultPasswordHash, "Sam Admin", Set.of(adminRole));

        log.info("Demo user seed completed.");
    }

    private RoleEntity getOrCreateRole(RoleName name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new RoleEntity(name, description)));
    }

    private void getOrCreateUser(String email, String passwordHash, String fullName, Set<RoleEntity> roles) {
        if (!userRepository.existsByEmail(email)) {
            UserEntity user = new UserEntity(email, passwordHash, fullName);
            user.setRoles(roles);
            userRepository.save(user);
            log.info("Seeded demo user: {}", email);
        }
    }
}
