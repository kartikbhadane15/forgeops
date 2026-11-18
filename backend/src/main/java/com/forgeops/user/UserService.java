package com.forgeops.user;

import com.forgeops.common.exception.BusinessRuleException;
import com.forgeops.common.exception.ResourceNotFoundException;
import com.forgeops.user.dto.CreateUserRequest;
import com.forgeops.user.dto.UpdateRolesRequest;
import com.forgeops.user.dto.UpdateUserStatusRequest;
import com.forgeops.user.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> listUsers(String search, Pageable pageable) {
        Page<UserEntity> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search.trim(), search.trim(), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(UserDTO::fromEntity);
    }

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BusinessRuleException("EMAIL_ALREADY_EXISTS", "A user with this email address already exists.");
        }

        UserEntity user = new UserEntity(
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName().trim()
        );

        Set<RoleEntity> roles = resolveRoles(request.getRoles());
        user.setRoles(roles);

        UserEntity savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }

    @Transactional
    public UserDTO updateRoles(UUID userId, UpdateRolesRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<RoleEntity> roles = resolveRoles(request.getRoles());
        user.setRoles(roles);

        UserEntity updated = userRepository.save(user);
        return UserDTO.fromEntity(updated);
    }

    @Transactional
    public UserDTO updateStatus(UUID userId, UpdateUserStatusRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setEnabled(request.getEnabled());
        UserEntity updated = userRepository.save(user);
        return UserDTO.fromEntity(updated);
    }

    private Set<RoleEntity> resolveRoles(Set<String> roleNames) {
        Set<RoleEntity> roles = new HashSet<>();
        for (String roleNameStr : roleNames) {
            String normalized = roleNameStr.startsWith("ROLE_") ? roleNameStr : "ROLE_" + roleNameStr;
            try {
                RoleName roleName = RoleName.valueOf(normalized);
                RoleEntity role = roleRepository.findByName(roleName)
                        .orElseGet(() -> roleRepository.save(new RoleEntity(roleName, "Role for " + roleName.name())));
                roles.add(role);
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException("INVALID_ROLE", "Unrecognized role name: " + roleNameStr);
            }
        }
        return roles;
    }
}
