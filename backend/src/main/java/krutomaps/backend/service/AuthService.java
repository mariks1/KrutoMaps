package krutomaps.backend.service;

import krutomaps.backend.dto.JwtAuthenticationResponseDTO;
import krutomaps.backend.dto.SignInRequestDTO;
import krutomaps.backend.dto.SignUpRequestDTO;
import krutomaps.backend.entity.RoleEntity;
import krutomaps.backend.entity.UserEntity;
import krutomaps.backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String DEFAULT_ROLE = "USER";

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;

    @Transactional(rollbackFor = Exception.class)
    public JwtAuthenticationResponseDTO signUp(SignUpRequestDTO request) {
        RoleEntity roleEntity = roleRepository
                .findByRoleName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + DEFAULT_ROLE));

        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(roleEntity)
                .build();

        userService.create(user);

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponseDTO(jwt, false);
    }

    public JwtAuthenticationResponseDTO signIn(SignInRequestDTO request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        UserEntity user = (UserEntity) userService
                .userDetailsService()
                .loadUserByUsername(request.getUsername());

        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());

        String jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponseDTO(jwt, isAdmin);
    }
}
