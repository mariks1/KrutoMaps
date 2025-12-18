package krutomaps.backend.service;

import krutomaps.backend.dto.PlaceMarkerDTO;
import krutomaps.backend.dto.RealtySelectionRequestDTO;
import krutomaps.backend.dto.RealtySelectionResponseDTO;
import krutomaps.backend.entity.PlaceEntity;
import krutomaps.backend.entity.RealtyEntity;
import krutomaps.backend.mapper.PlaceMapper;
import krutomaps.backend.mapper.RealtyMapper;
import krutomaps.backend.repository.PlaceRepository;
import krutomaps.backend.repository.RealtyRepository;
import krutomaps.backend.repository.RealtyScoreRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealtyServiceUnitTest {

    @Mock
    private RealtyRepository realtyRepository;
    @Mock
    private PlaceRepository placeRepository;

    private PlaceMapper placeMapper;
    private RealtyMapper realtyMapper;

    @InjectMocks
    private RealtyService realtyService;

    @BeforeEach
    void setUp() {
        placeMapper = Mappers.getMapper(PlaceMapper.class);
        realtyMapper = Mappers.getMapper(RealtyMapper.class);
        realtyService = new RealtyService(realtyRepository, placeRepository, placeMapper, realtyMapper);
    }

    @Test
    void selectsTop5AndMapsPlaces() {
        RealtySelectionRequestDTO request = RealtySelectionRequestDTO.builder()
                .priceFrom(1000)
                .priceTo(5000)
                .areaFrom(10)
                .areaTo(200)
                .wantToSee(List.of("cafe"))
                .dontWantToSee(List.of("bar"))
                .placeOptions(List.of("A", "B"))
                .build();

        when(realtyRepository.findTop5Scored(any(), any(), any(), any(), any(), any(), any(), anyDouble()))
                .thenReturn(List.of(
                        row(2L, 9.0),
                        row(1L, 5.0),
                        row(3L, 1.0)
                ));

        when(realtyRepository.findAllById(anyCollection()))
                .thenReturn(List.of(
                        realty(1L, "addr1", "SEG1"),
                        realty(2L, "addr2", "SEG2"),
                        realty(3L, "addr3", "SEG3")
                ));

        when(placeRepository.findByRubrics(any(String[].class)))
                .thenReturn(List.of(
                        place(10L, "Cafe A", "addr", 10.0, 20.0, List.of("cafe")),
                        place(11L, "Bar B", "addr", 11.0, 21.0, List.of("bar"))
                ));

        RealtySelectionResponseDTO response = realtyService.findTop5ByCriteria(request);

        assertThat(response.getRealtyEntityList())
                .extracting("id")
                .containsExactly(2L, 1L, 3L);

        assertThat(response.getPreferredPlaces())
                .hasSize(2)
                .allMatch(p -> p.getMarkerType() == PlaceMarkerDTO.MarkerType.PREFERRED);
        assertThat(response.getAvoidedPlaces())
                .hasSize(2)
                .allMatch(p -> p.getMarkerType() == PlaceMarkerDTO.MarkerType.AVOIDED);

        ArgumentCaptor<String[]> segmentCaptor = ArgumentCaptor.forClass(String[].class);
        verify(realtyRepository).findTop5Scored(
                eq(1000.0), eq(5000.0), eq(10.0), eq(200.0),
                segmentCaptor.capture(),
                any(String[].class), any(String[].class), anyDouble());
        assertThat(segmentCaptor.getValue()).containsExactly("A", "B");
    }

    @Test
    void passesNullsWhenFiltersMissing() {
        RealtySelectionRequestDTO request = RealtySelectionRequestDTO.builder()
                .placeOptions(List.of("گ>‘?گ+گ?گü")) // special any option
                .build();

        when(realtyRepository.findTop5Scored(
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), anyDouble()))
                .thenReturn(List.of());

        RealtySelectionResponseDTO response = realtyService.findTop5ByCriteria(request);

        assertThat(response.getRealtyEntityList()).isEmpty();
        assertThat(response.getPreferredPlaces()).isEmpty();
        assertThat(response.getAvoidedPlaces()).isEmpty();
    }

    @Test
    void returnsRangeDtos() {
        when(realtyRepository.findMinPrice()).thenReturn(10.0);
        when(realtyRepository.findMaxPrice()).thenReturn(100.0);
        when(realtyRepository.findMinArea()).thenReturn(5.0);
        when(realtyRepository.findMaxArea()).thenReturn(50.0);

        assertThat(realtyService.getPriceRange())
                .extracting("minPrice", "maxPrice")
                .containsExactly(10.0, 100.0);
        assertThat(realtyService.getAreaRange())
                .extracting("minArea", "maxArea")
                .containsExactly(5.0, 50.0);
    }

    private RealtyScoreRow row(Long id, Double score) {
        return new RealtyScoreRow() {
            @Override public Long getId() { return id; }
            @Override public Double getScore() { return score; }
        };
    }

    private RealtyEntity realty(Long id, String address, String segment) {
        RealtyEntity re = new RealtyEntity();
        re.setId(id);
        re.setAddress(address);
        re.setSegmentType(segment);
        re.setMainType("MAIN");
        re.setLeasePrice(123.0);
        re.setPointX(30.0);
        re.setPointY(60.0);
        re.setTotalArea(50.0);
        re.setUpdateDate(LocalDate.now());
        re.setSquareNum(1L);
        re.setEntityName("ent");
        re.setAdditionalInfo("add");
        re.setSourceInfo("src");
        return re;
    }

    private PlaceEntity place(Long id, String name, String addr, double lat, double lon, List<String> rubrics) {
        PlaceEntity p = new PlaceEntity();
        p.setId(id);
        p.setName(name);
        p.setAddress(addr);
        p.setLat(lat);
        p.setLon(lon);
        p.setRubrics(rubrics);
        return p;
    }
}
