package com.taskmanagement.service;

import com.taskmanagement.dto.UserCreateDTO;
import com.taskmanagement.dto.UserDTO;
import com.taskmanagement.exception.DuplicateResourceException;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.model.AuditLog;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        log.info("Creating user: {}", userCreateDTO.getUsername());

        // Check for duplicates
        if (userRepository.existsByUsername(userCreateDTO.getUsername())) {
            throw new DuplicateResourceException("User", "username", userCreateDTO.getUsername());
        }
        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new DuplicateResourceException("User", "email", userCreateDTO.getEmail());
        }

        User user = User.builder()
                .username(userCreateDTO.getUsername())
                .email(userCreateDTO.getEmail())
                .password(passwordEncoder.encode(userCreateDTO.getPassword()))
                .firstName(userCreateDTO.getFirstName())
                .lastName(userCreateDTO.getLastName())
                .phoneNumber(userCreateDTO.getPhoneNumber())
                .role(userCreateDTO.getRole())
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        // Audit log
        auditLogService.log("USER", savedUser.getId(), AuditLog.AuditAction.CREATE,
                "SYSTEM", null, savedUser.getUsername(), "User created");

        log.info("User created successfully: {}", savedUser.getUsername());
        return modelMapper.map(savedUser, UserDTO.class);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return modelMapper.map(user, UserDTO.class);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return modelMapper.map(user, UserDTO.class);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        String oldValue = user.toString();

        // Update fields
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new DuplicateResourceException("User", "email", userDTO.getEmail());
            }
            user.setEmail(userDTO.getEmail());
        }

        if (userDTO.getFirstName() != null) user.setFirstName(userDTO.getFirstName());
        if (userDTO.getLastName() != null) user.setLastName(userDTO.getLastName());
        if (userDTO.getPhoneNumber() != null) user.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getRole() != null) user.setRole(userDTO.getRole());
        if (userDTO.getActive() != null) user.setActive(userDTO.getActive());

        User updatedUser = userRepository.save(user);

        // Audit log
        auditLogService.log("USER", updatedUser.getId(), AuditLog.AuditAction.UPDATE,
                "SYSTEM", oldValue, updatedUser.toString(), "User updated");

        log.info("User updated successfully: {}", updatedUser.getUsername());
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userRepository.delete(user);

        // Audit log
        auditLogService.log("USER", id, AuditLog.AuditAction.DELETE,
                "SYSTEM", user.getUsername(), null, "User deleted");

        log.info("User deleted successfully: {}", id);
    }

    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}

