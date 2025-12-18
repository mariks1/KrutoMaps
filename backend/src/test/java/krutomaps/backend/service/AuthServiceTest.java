package krutomaps.backend.service;

import krutomaps.backend.dto.JwtAuthenticationResponseDTO;
import krutomaps.backend.dto.SignInRequestDTO;
import krutomaps.backend.dto.SignUpRequestDTO;
import krutomaps.backend.entity.RoleEntity;
import krutomaps.backend.entity.UserEntity;
import krutomaps.backend.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserService userService;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RoleRepository roleRepository;

    @InjectMocks
    private AuthService authService;

    private RoleEntity userRole;

    @BeforeEach
    void init() {
        userRole = RoleEntity.builder().roleId(1L).roleName("USER").build();
    }

    @Test
    void signUpAssignsDefaultRoleAndReturnsToken() {
        SignUpRequestDTO req = SignUpRequestDTO.builder()
                .username("bob123")
                .password("secretpass")
                .role("ADMIN")
                .build();

        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("secretpass")).thenReturn("hashed");
        when(jwtService.generateToken(any(UserEntity.class))).thenReturn("jwt-token");

        JwtAuthenticationResponseDTO resp = authService.signUp(req);

        assertThat(resp.getToken()).isEqualTo("jwt-token");
        assertThat(resp.isAdminRole()).isFalse();

        verify(userService).create(argThat(user ->
                user.getUsername().equals("bob123") &&
                user.getPassword().equals("hashed") &&
                user.getRole().equals(userRole)
        ));
    }

    @Test
    void signInAuthenticatesAndMarksAdminFlag() {
        SignInRequestDTO req = new SignInRequestDTO("alice99", "pw123456");

        UserEntity user = UserEntity.builder()
                .id(5L)
                .username("alice99")
                .password("encoded")
                .role(RoleEntity.builder().roleName("ADMIN").build())
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        UserDetailsService uds = username -> user;
        when(userService.userDetailsService()).thenReturn(uds);
        when(jwtService.generateToken(user)).thenReturn("token-admin");

        JwtAuthenticationResponseDTO resp = authService.signIn(req);

        assertThat(resp.getToken()).isEqualTo("token-admin");
        assertThat(resp.isAdminRole()).isTrue();
    }
}
