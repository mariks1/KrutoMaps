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
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;


    @Transactional(rollbackFor = Exception.class)
    public JwtAuthenticationResponseDTO signUp(SignUpRequestDTO request) {

        RoleEntity roleEntity = roleRepository
                .findByRoleName(request.getRole())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Роль не найдена: " + request.getRole()));

        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(roleEntity)
                .build();

        userService.create(user);

        var jwt = jwtService.generateToken(user);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(request.getRole());
        return new JwtAuthenticationResponseDTO(jwt, isAdmin);
    }

    /**
     * Аутентификация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponseDTO signIn(SignInRequestDTO request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        UserEntity user = (UserEntity) userService
                .userDetailsService()
                .loadUserByUsername(request.getUsername());

        boolean isAdmin = "ADMIN".equalsIgnoreCase(
                user.getRole().getRoleName()
        );

        String jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponseDTO(jwt, isAdmin);
    }

}