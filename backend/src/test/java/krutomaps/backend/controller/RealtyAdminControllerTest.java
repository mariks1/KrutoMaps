package krutomaps.backend.controller;

import krutomaps.backend.entity.RealtyEntity;
import krutomaps.backend.repository.RealtyRepository;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = RealtyAdminController.class)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RealtyAdminController.class, RealtyAdminControllerTest.TestSecurityConfig.class})
class RealtyAdminControllerTest {

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
    private RealtyRepository realtyRepository;

    @Test
    @WithMockUser(authorities = "ADMIN")
    void adminCanList() throws Exception {
        when(realtyRepository.findAll()).thenReturn(List.of(sampleRealty(1L)));

        mockMvc.perform(MockMvcRequestBuilders.get("/admin/realty"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void nonAdminForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/realty"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void adminCanGet() throws Exception {
        when(realtyRepository.findById(1L)).thenReturn(Optional.of(sampleRealty(1L)));

        mockMvc.perform(MockMvcRequestBuilders.get("/admin/realty/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void adminCanCreate() throws Exception {
        RealtyEntity saved = sampleRealty(2L);
        when(realtyRepository.save(any(RealtyEntity.class))).thenReturn(saved);

        String body = """
            {
              "pointX":30.0,
              "pointY":60.0,
              "mainType":"MAIN",
              "segmentType":"SEG",
              "entityName":"Entity",
              "totalArea":50.0,
              "leasePrice":1000.0,
              "additionalInfo":"add",
              "sourceInfo":"src",
              "address":"Addr",
              "updateDate":"2024-01-01",
              "squareNum":1
            }
            """;

        mockMvc.perform(MockMvcRequestBuilders.post("/admin/realty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private RealtyEntity sampleRealty(Long id) {
        RealtyEntity e = new RealtyEntity();
        e.setId(id);
        e.setPointX(30.0);
        e.setPointY(60.0);
        e.setMainType("MAIN");
        e.setSegmentType("SEG");
        e.setEntityName("Entity");
        e.setTotalArea(50.0);
        e.setLeasePrice(1000.0);
        e.setAdditionalInfo("add");
        e.setSourceInfo("src");
        e.setAddress("Addr");
        e.setUpdateDate(LocalDate.now());
        e.setSquareNum(1L);
        return e;
    }
}
