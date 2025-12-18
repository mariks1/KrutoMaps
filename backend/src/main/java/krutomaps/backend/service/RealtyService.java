package krutomaps.backend.service;

import krutomaps.backend.dto.AreaRangeResponseDTO;
import krutomaps.backend.dto.PlaceMarkerDTO;
import krutomaps.backend.dto.PriceRangeResponseDTO;
import krutomaps.backend.dto.RealtySelectionRequestDTO;
import krutomaps.backend.dto.RealtySelectionResponseDTO;
import krutomaps.backend.dto.RealtySummaryDTO;
import krutomaps.backend.mapper.PlaceMapper;
import krutomaps.backend.mapper.RealtyMapper;
import krutomaps.backend.repository.PlaceRepository;
import krutomaps.backend.repository.RealtyRepository;
import krutomaps.backend.repository.RealtyScoreRow;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RealtyService {

    private static final double SEARCH_RADIUS_METERS = 2000.0;

    private final RealtyRepository realtyRepository;
    private final PlaceRepository placeRepository;
    private final PlaceMapper placeMapper;
    private final RealtyMapper realtyMapper;

    @Cacheable("priceRange")
    public PriceRangeResponseDTO getPriceRange() {
        return PriceRangeResponseDTO.builder()
                .minPrice(realtyRepository.findMinPrice())
                .maxPrice(realtyRepository.findMaxPrice())
                .build();
    }

    @Cacheable("areaRange")
    public AreaRangeResponseDTO getAreaRange() {
        return AreaRangeResponseDTO.builder()
                .minArea(realtyRepository.findMinArea())
                .maxArea(realtyRepository.findMaxArea())
                .build();
    }

    public RealtySelectionResponseDTO findTop5ByCriteria(RealtySelectionRequestDTO request) {
        String[] want = toArray(request.getWantToSee());
        String[] avoid = toArray(request.getDontWantToSee());
        String[] segmentTypes = getSegmentTypes(request.getPlaceOptions());

        List<RealtyScoreRow> rows = realtyRepository.findTop5Scored(
                request.getPriceFrom() == null ? null : request.getPriceFrom().doubleValue(),
                request.getPriceTo() == null ? null : request.getPriceTo().doubleValue(),
                request.getAreaFrom() == null ? null : request.getAreaFrom().doubleValue(),
                request.getAreaTo() == null ? null : request.getAreaTo().doubleValue(),
                segmentTypes,
                want,
                avoid,
                SEARCH_RADIUS_METERS
        );

        Map<Long, Double> scoresById = rows.stream()
                .collect(Collectors.toMap(RealtyScoreRow::getId, RealtyScoreRow::getScore));

        List<RealtySummaryDTO> top5 = realtyRepository.findAllById(scoresById.keySet()).stream()
                .map(realtyMapper::toSummary)
                .sorted(Comparator.comparingDouble(dto -> -scoresById.getOrDefault(dto.getId(), 0.0)))
                .limit(5)
                .toList();

        return RealtySelectionResponseDTO.builder()
                .realtyEntityList(top5)
                .preferredPlaces(getPlacesForRubrics(request.getWantToSee(), PlaceMarkerDTO.MarkerType.PREFERRED))
                .avoidedPlaces(getPlacesForRubrics(request.getDontWantToSee(), PlaceMarkerDTO.MarkerType.AVOIDED))
                .build();
    }

    private List<PlaceMarkerDTO> getPlacesForRubrics(List<String> rubrics, PlaceMarkerDTO.MarkerType type) {
        if (rubrics == null || rubrics.isEmpty()) return List.of();
        String[] rubricsArray = rubrics.toArray(new String[0]);
        return placeRepository.findByRubrics(rubricsArray).stream()
                .map(p -> placeMapper.toMarkerDTO(p, type))
                .toList();
    }

    private String[] toArray(List<String> src) {
        return (src == null || src.isEmpty()) ? null : src.toArray(new String[0]);
    }

    private String[] getSegmentTypes(List<String> placeOptions) {
        if (placeOptions == null || placeOptions.isEmpty()) return null;
        // Preserve behavior: do not filter if the "any" option is present.
        if (placeOptions.contains("گ>‘?گ+گ?گü")) return null;
        return placeOptions.toArray(new String[0]);
    }
}
