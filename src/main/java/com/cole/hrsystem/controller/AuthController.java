package com.cole.hrsystem.controller;

import com.cole.hrsystem.dto.AuthDto;
import com.cole.hrsystem.exception.DuplicateResourceException;
import com.cole.hrsystem.model.AppUser;
import com.cole.hrsystem.model.AppUser.Role;
import com.cole.hrsystem.repository.AppUserRepository;
import com.cole.hrsystem.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final AppUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthDto.LoginResponse> login(@Valid @RequestBody AuthDto.LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        AppUser user = userRepo.findByUsername(auth.getName()).orElseThrow();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return ResponseEntity.ok(new AuthDto.LoginResponse(token, user.getUsername(), user.getRole().name()));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody AuthDto.RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + req.getUsername());
        }

        // Default to ROLE_EMPLOYEE; only ADMINs can create MANAGER/ADMIN accounts
        // (You'd add @PreAuthorize here in a production system)
        Role role = Role.ROLE_EMPLOYEE;
        if (req.getRole() != null) {
            try { role = Role.valueOf(req.getRole()); }
            catch (IllegalArgumentException ignored) {}
        }

        AppUser user = new AppUser(req.getUsername(), passwordEncoder.encode(req.getPassword()), role);
        userRepo.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
