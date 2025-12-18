package krutomaps.backend.controller;

import krutomaps.backend.entity.PlaceEntity;
import krutomaps.backend.repository.PlaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = PlaceAdminController.class)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PlaceAdminController.class, PlaceAdminControllerTest.TestSecurityConfig.class})
class PlaceAdminControllerTest {

    @Configuration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceRepository placeRepository;

    @Test
    @WithMockUser(authorities = "ADMIN")
    void adminCanList() throws Exception {
        when(placeRepository.findAll()).thenReturn(List.of(samplePlace(1L)));

        mockMvc.perform(MockMvcRequestBuilders.get("/admin/places"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void nonAdminForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/places"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void adminCanGet() throws Exception {
        when(placeRepository.findById(1L)).thenReturn(Optional.of(samplePlace(1L)));

        mockMvc.perform(MockMvcRequestBuilders.get("/admin/places/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void adminCanCreate() throws Exception {
        PlaceEntity saved = samplePlace(2L);
        when(placeRepository.save(any(PlaceEntity.class))).thenReturn(saved);

        String body = """
            {
              "name":"Cafe",
              "address":"Addr",
              "lat":1.0,
              "lon":2.0,
              "rubrics":["cafe"]
            }
            """;

        mockMvc.perform(MockMvcRequestBuilders.post("/admin/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private PlaceEntity samplePlace(Long id) {
        PlaceEntity p = new PlaceEntity();
        p.setId(id);
        p.setName("name");
        p.setAddress("addr");
        p.setLat(1.0);
        p.setLon(2.0);
        p.setRubrics(List.of("tag"));
        return p;
    }
}
