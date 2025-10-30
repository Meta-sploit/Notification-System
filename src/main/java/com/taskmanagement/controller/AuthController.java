package com.taskmanagement.controller;

import com.taskmanagement.dto.AuthenticationRequest;
import com.taskmanagement.dto.AuthenticationResponse;
import com.taskmanagement.dto.UserCreateDTO;
import com.taskmanagement.dto.UserDTO;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.security.JwtUtil;
import com.taskmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication and registration")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        // Create the user
        UserDTO createdUser = userService.createUser(userCreateDTO);
        
        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(createdUser.getUsername());
        
        // Generate JWT token
        String token = jwtUtil.generateToken(userDetails, createdUser.getRole().name());
        
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(token)
                .username(createdUser.getUsername())
                .email(createdUser.getEmail())
                .role(createdUser.getRole().name())
                .message("User registered successfully")
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get JWT token")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest authRequest) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        User user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        
        // Generate JWT token
        String token = jwtUtil.generateToken(userDetails, user.getRole().name());
        
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
        
        return ResponseEntity.ok(response);
    }
}

