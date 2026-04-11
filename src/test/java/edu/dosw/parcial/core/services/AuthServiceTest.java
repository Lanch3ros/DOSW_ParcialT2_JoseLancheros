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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    // ── register ────────────────────────────────────────────────────────────

    @Test
    void register_newUser_returnsUserResponse() {
        RegisterRequest request = buildRegisterRequest("nuevo@eci.edu.co");
        UserEntity saved = buildUser(1L, "nuevo@eci.edu.co");
        UserResponse expected = UserResponse.builder().id("1").email("nuevo@eci.edu.co").build();

        when(userRepository.existsByEmail("nuevo@eci.edu.co")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(saved);
        when(userMapper.toResponse(saved)).thenReturn(expected);

        UserResponse result = authService.register(request);

        assertThat(result).isEqualTo(expected);
        verify(userRepository).save(argThat(u ->
                u.getRole() == UserRole.ROLE_CLIENT &&
                u.getEmail().equals("nuevo@eci.edu.co")));
    }

    @Test
    void register_duplicateEmail_throwsIllegalArgument() {
        RegisterRequest request = buildRegisterRequest("existente@eci.edu.co");
        when(userRepository.existsByEmail("existente@eci.edu.co")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un usuario con el email");

        verify(userRepository, never()).save(any());
    }

    // ── login ───────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returnsAuthResponse() {
        LoginRequest request = buildLoginRequest("cliente@eci.edu.co", "Password1");
        UserEntity user = buildUser(1L, "cliente@eci.edu.co");

        when(userRepository.findByEmail("cliente@eci.edu.co")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1", "hashed_password")).thenReturn(true);
        when(jwtUtil.generateToken("cliente@eci.edu.co", "ROLE_CLIENT")).thenReturn("jwt.token.here");
        when(jwtUtil.getExpiration()).thenReturn(86400L);

        AuthResponse result = authService.login(request);

        assertThat(result.getToken()).isEqualTo("jwt.token.here");
        assertThat(result.getRole()).isEqualTo("ROLE_CLIENT");
        assertThat(result.getUserId()).isEqualTo("1");
    }

    @Test
    void login_userNotFound_throwsIllegalArgument() {
        LoginRequest request = buildLoginRequest("noexiste@eci.edu.co", "Password1");
        when(userRepository.findByEmail("noexiste@eci.edu.co")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciales inválidas");
    }

    @Test
    void login_wrongPassword_throwsIllegalArgument() {
        LoginRequest request = buildLoginRequest("cliente@eci.edu.co", "WrongPass1");
        UserEntity user = buildUser(1L, "cliente@eci.edu.co");

        when(userRepository.findByEmail("cliente@eci.edu.co")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass1", "hashed_password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciales inválidas");
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private RegisterRequest buildRegisterRequest(String email) {
        RegisterRequest r = new RegisterRequest();
        r.setFullName("Test User");
        r.setEmail(email);
        r.setPassword("Password1");
        return r;
    }

    private LoginRequest buildLoginRequest(String email, String password) {
        LoginRequest r = new LoginRequest();
        r.setEmail(email);
        r.setPassword(password);
        return r;
    }

    private UserEntity buildUser(Long id, String email) {
        return UserEntity.builder()
                .id(id)
                .fullName("Test User")
                .email(email)
                .password("hashed_password")
                .role(UserRole.ROLE_CLIENT)
                .build();
    }
}
