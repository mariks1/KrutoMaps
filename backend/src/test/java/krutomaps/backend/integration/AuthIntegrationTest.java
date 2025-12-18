package krutomaps.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import krutomaps.backend.dto.JwtAuthenticationResponseDTO;
import krutomaps.backend.dto.SignInRequestDTO;
import krutomaps.backend.dto.SignUpRequestDTO;
import krutomaps.backend.entity.RoleEntity;
import krutomaps.backend.entity.UserEntity;
import krutomaps.backend.repository.RoleRepository;
import krutomaps.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class AuthIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        roleRepository.save(RoleEntity.builder().roleName("USER").build());
        roleRepository.save(RoleEntity.builder().roleName("ADMIN").build());
    }

    @Test
    void signUpCreatesUserWithUserRole() throws Exception {
        SignUpRequestDTO req = SignUpRequestDTO.builder()
                .username("newuser1")
                .password("password123")
                .role("ADMIN")
                .build();

        String response = mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adminRole").value(false))
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        JwtAuthenticationResponseDTO dto = objectMapper.readValue(response, JwtAuthenticationResponseDTO.class);
        assertThat(dto.getToken()).isNotBlank();
        assertThat(dto.isAdminRole()).isFalse();

        UserEntity saved = userRepository.findByUsername("newuser1").orElseThrow();
        assertThat(saved.getRole().getRoleName()).isEqualTo("USER");
        assertThat(passwordEncoder.matches("password123", saved.getPassword())).isTrue();
    }

    @Test
    void signInReturnsToken() throws Exception {
        RoleEntity userRole = roleRepository.findByRoleName("USER").orElseThrow();
        UserEntity user = UserEntity.builder()
                .username("loginuser")
                .password(passwordEncoder.encode("pass12345"))
                .role(userRole)
                .build();
        userRepository.save(user);

        SignInRequestDTO req = SignInRequestDTO.builder()
                .username("loginuser")
                .password("pass12345")
                .build();

        mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.adminRole").value(false));
    }
}
