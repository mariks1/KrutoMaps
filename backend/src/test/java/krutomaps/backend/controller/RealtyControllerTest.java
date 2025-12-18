package krutomaps.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import krutomaps.backend.configuration.SecurityConfiguration;
import krutomaps.backend.dto.AreaRangeResponseDTO;
import krutomaps.backend.dto.PriceRangeResponseDTO;
import krutomaps.backend.dto.RealtySelectionRequestDTO;
import krutomaps.backend.dto.RealtySelectionResponseDTO;
import krutomaps.backend.filter.JwtAuthenticationFilter;
import krutomaps.backend.service.RealtyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = RealtyController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfiguration.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
class RealtyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RealtyService realtyService;

    @Test
    void selectionReturnsResponseWhenFound() throws Exception {
        RealtySelectionResponseDTO resp = RealtySelectionResponseDTO.builder()
                .realtyEntityList(List.of(
                        krutomaps.backend.dto.RealtySummaryDTO.builder()
                                .id(1L).address("a").mainType("m").segmentType("s")
                                .leasePrice(1.0).latitude(1.0).longitude(1.0).totalArea(10.0)
                                .build()
                ))
                .preferredPlaces(List.of())
                .avoidedPlaces(List.of())
                .build();
        when(realtyService.findTop5ByCriteria(any(RealtySelectionRequestDTO.class))).thenReturn(resp);

        RealtySelectionRequestDTO req = RealtySelectionRequestDTO.builder()
                .priceFrom(100)
                .priceTo(1000)
                .build();

        mockMvc.perform(post("/api/selection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.realtyEntityList").isArray());
    }

    @Test
    void selectionReturnsNoContentWhenEmpty() throws Exception {
        RealtySelectionResponseDTO resp = RealtySelectionResponseDTO.builder()
                .realtyEntityList(List.of())
                .preferredPlaces(List.of())
                .avoidedPlaces(List.of())
                .build();
        when(realtyService.findTop5ByCriteria(any(RealtySelectionRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(post("/api/selection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RealtySelectionRequestDTO.builder().build())))
                .andExpect(status().isNoContent());
    }

    @Test
    void priceRangeReturnsDto() throws Exception {
        when(realtyService.getPriceRange()).thenReturn(PriceRangeResponseDTO.builder()
                .minPrice(1.0).maxPrice(5.0).build());

        mockMvc.perform(get("/api/price-range"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minPrice").value(1.0))
                .andExpect(jsonPath("$.maxPrice").value(5.0));
    }

    @Test
    void areaRangeReturnsDto() throws Exception {
        when(realtyService.getAreaRange()).thenReturn(AreaRangeResponseDTO.builder()
                .minArea(10.0).maxArea(50.0).build());

        mockMvc.perform(get("/api/area-range"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minArea").value(10.0))
                .andExpect(jsonPath("$.maxArea").value(50.0));
    }
}
