package edu.dosw.parcial.core.services;

import edu.dosw.parcial.controller.dtos.request.LoginRequest;
import edu.dosw.parcial.controller.dtos.request.RegisterRequest;
import edu.dosw.parcial.controller.dtos.response.AuthResponse;
import edu.dosw.parcial.controller.dtos.response.UserResponse;
import edu.dosw.parcial.controller.mappers.UserMapper;
import edu.dosw.parcial.core.models.UserRole;
import edu.dosw.parcial.core.utils.JwtUtil;
import edu.dosw.parcial.persistence.entities.UserEntity;
import edu.dosw.parcial.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public UserResponse register(RegisterRequest request) {
        log.info("Registrando nuevo usuario con email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + request.getEmail());
        }

        UserEntity user = UserEntity.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.ROLE_CLIENT)
                .build();

        UserEntity saved = userRepository.save(user);
        log.info("Usuario registrado exitosamente con id: {}", saved.getId());

        return userMapper.toResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Intento de login para email: {}", request.getEmail());

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        log.info("Login exitoso para usuario id: {}", user.getId());

        return AuthResponse.builder()
                .token(token)
                .expiresIn(jwtUtil.getExpiration())
                .userId(String.valueOf(user.getId()))
                .role(user.getRole().name())
                .build();
    }
}
