package krutomaps.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import krutomaps.backend.dto.JwtAuthenticationResponseDTO;
import krutomaps.backend.dto.SignInRequestDTO;
import krutomaps.backend.dto.SignUpRequestDTO;
import krutomaps.backend.entity.PlaceEntity;
import krutomaps.backend.entity.RealtyEntity;
import krutomaps.backend.entity.RoleEntity;
import krutomaps.backend.repository.PlaceRepository;
import krutomaps.backend.repository.RealtyRepository;
import krutomaps.backend.repository.RoleRepository;
import krutomaps.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@ExtendWith(SpringExtension.class)
class SystemFlowTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgis/postgis:16-3.5")
                    .asCompatibleSubstituteFor("postgres"))
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("csv/realty.csv"),
                            "/csv/realty.csv"
                    )
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("csv/poi.csv"),
                            "/csv/poi.csv"
                    )
                    .withDatabaseName("kruto")
                    .withUsername("test")
                    .withPassword("test");;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> true);
    }



    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PlaceRepository placeRepository;
    @Autowired private RealtyRepository realtyRepository;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        realtyRepository.deleteAll();
        placeRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        roleRepository.save(RoleEntity.builder().roleName("USER").build());
        roleRepository.save(RoleEntity.builder().roleName("ADMIN").build());

        PlaceEntity place = new PlaceEntity();
        place.setName("Cafe A");
        place.setAddress("addr1");
        place.setLat(10.0);
        place.setLon(20.0);
        place.setRubrics(java.util.List.of("cafe", "food"));
        placeRepository.save(place);

        RealtyEntity realty = new RealtyEntity();
        realty.setPointX(30.0);
        realty.setPointY(60.0);
        realty.setMainType("MAIN");
        realty.setSegmentType("SEG");
        realty.setEntityName("Entity");
        realty.setTotalArea(50.0);
        realty.setLeasePrice(1000.0);
        realty.setAdditionalInfo("add");
        realty.setSourceInfo("src");
        realty.setAddress("Addr");
        realty.setUpdateDate(LocalDate.now());
        realty.setSquareNum(1L);
        realtyRepository.save(realty);
    }

    @Test
    void fullFlow_signUp_signIn_and_accessProtectedEndpoints() throws Exception {
        SignUpRequestDTO signUp = SignUpRequestDTO.builder()
                .username("flowuser")
                .password("password123")
                .role("ADMIN")
                .build();

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUp)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adminRole").value(false))
                .andExpect(jsonPath("$.token").exists());

        SignInRequestDTO signIn = SignInRequestDTO.builder()
                .username("flowuser")
                .password("password123")
                .build();

        String signInResponse = mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signIn)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JwtAuthenticationResponseDTO jwt = objectMapper.readValue(signInResponse, JwtAuthenticationResponseDTO.class);
        assertThat(jwt.getToken()).isNotBlank();

        mockMvc.perform(get("/api/rubrics")
                        .header("Authorization", "Bearer " + jwt.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("cafe"));

        mockMvc.perform(get("/api/price-range")
                        .header("Authorization", "Bearer " + jwt.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minPrice").value(1000.0))
                .andExpect(jsonPath("$.maxPrice").value(1000.0));

        // Main selection flow with rubrics
        String selectionBody = """
            {
              "priceFrom":500,
              "priceTo":1500,
              "wantToSee":["cafe"]
            }
            """;

        mockMvc.perform(post("/api/selection")
                        .header("Authorization", "Bearer " + jwt.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(selectionBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.realtyEntityList[0].leasePrice").value(1000.0))
                .andExpect(jsonPath("$.realtyEntityList[0].address").value("Addr"));
    }
}
