package krutomaps.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import krutomaps.backend.dto.JwtAuthenticationResponseDTO;
import krutomaps.backend.dto.SignInRequestDTO;
import krutomaps.backend.dto.SignUpRequestDTO;
import krutomaps.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import krutomaps.backend.configuration.SecurityConfiguration;
import krutomaps.backend.filter.JwtAuthenticationFilter;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfiguration.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void signInReturnsJwt() throws Exception {
        when(authService.signIn(any(SignInRequestDTO.class)))
                .thenReturn(new JwtAuthenticationResponseDTO("token-1", true));

        SignInRequestDTO req = SignInRequestDTO.builder()
                .username("user1")
                .password("pass1234")
                .build();

        mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-1"))
                .andExpect(jsonPath("$.adminRole").value(true));
    }

    @Test
    void signUpReturnsJwt() throws Exception {
        when(authService.signUp(any(SignUpRequestDTO.class)))
                .thenReturn(new JwtAuthenticationResponseDTO("token-2", false));

        SignUpRequestDTO req = SignUpRequestDTO.builder()
                .username("user2")
                .password("pass5678")
                .role("ADMIN")
                .build();

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-2"))
                .andExpect(jsonPath("$.adminRole").value(false));
    }
}
