package krutomaps.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import krutomaps.backend.dto.*;
import krutomaps.backend.entity.RealtyEntity;
import krutomaps.backend.entity.SquareEntity;
import krutomaps.backend.entity.SquareScoreEntity;
import krutomaps.backend.repository.PlaceRepository;
import krutomaps.backend.repository.RealtyRepository;
import krutomaps.backend.repository.SquareScoreRepository;
import krutomaps.backend.utils.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RealtyService {

    private final ObjectMapper mapper;

    private final RealtyRepository realtyRepository;
    private final PlaceRepository placeRepository;
    private final SquareScoreRepository squareScoreRepository;
    private final SquareService squareService;

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

        SquareEntity bestSquare = chooseBestSquare(request);
        Specification<RealtyEntity> spec = buildSpecification(request, bestSquare);

        List<RealtyEntity> candidates = realtyRepository.findAll(spec);
        List<RealtySummaryDTO> top5 = scoreAndPickTop5(candidates, request, bestSquare);

        return RealtySelectionResponseDTO.builder()
                .realtyEntityList(top5)
                .preferredPlaces(getPlacesForRubrics(request.getWantToSee(), PlaceMarkerDTO.MarkerType.PREFERRED))
                .avoidedPlaces(getPlacesForRubrics(request.getDontWantToSee(), PlaceMarkerDTO.MarkerType.AVOIDED))
                .build();
    }


    private SquareEntity chooseBestSquare(RealtySelectionRequestDTO request) {
        if ((request.getWantToSee() == null || request.getWantToSee().isEmpty()) &&
                (request.getDontWantToSee() == null || request.getDontWantToSee().isEmpty())) {
            return null;
        }
        return squareService.getAllSquares().stream()
                .max(Comparator.comparingDouble(square ->
                        safeCalc(() -> calcSquareScore(square, request))))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No squares found"));
    }

    private double calcSquareScore(SquareEntity square, RealtySelectionRequestDTO request) throws JsonProcessingException {

        SquareScoreEntity scoreEntity = squareScoreRepository
                .findById(square.getId())
                .orElseGet(() -> SquareScoreEntity.builder()
                        .id(square.getId())
                        .rubricScore("{}")
                        .build());

        Map<String, Double> rubricScores = mapper.readValue(
                scoreEntity.getRubricScore(),
                new TypeReference<>() {}
        );

        double totalScore = 0.0;

        if (request.getWantToSee() != null) {
            totalScore += request.getWantToSee().stream()
                    .mapToDouble(rubric -> rubricScores.getOrDefault(rubric, 0.0))
                    .sum();
        }

        if (request.getDontWantToSee() != null) {
            totalScore -= request.getDontWantToSee().stream()
                    .mapToDouble(rubric -> rubricScores.getOrDefault(rubric, 0.0))
                    .sum();
        }

        return totalScore;
    }

    private Specification<RealtyEntity> buildSpecification(RealtySelectionRequestDTO request, SquareEntity square) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();

            opt(request.getPriceFrom()).ifPresent(v -> preds.add(cb.ge(root.get("leasePrice"), v)));
            opt(request.getPriceTo()).ifPresent(v -> preds.add(cb.le(root.get("leasePrice"), v)));
            opt(request.getAreaFrom()).ifPresent(v -> preds.add(cb.ge(root.get("totalArea"), v)));
            opt(request.getAreaTo()).ifPresent(v -> preds.add(cb.le(root.get("totalArea"), v)));

            switch (Objects.toString(request.getFloorOption(), "")) {
                case "Только 1-й"  -> preds.add(cb.equal(root.get("floor"), 1));
                case "До 5-го"     -> preds.add(cb.le(root.get("floor"), 5));
                case "Не 1-й"      -> preds.add(cb.notEqual(root.get("floor"), 1));
                default -> { }
            }
            if (request.getPlaceOptions() != null && !request.getPlaceOptions().contains("Любой")) {
                preds.add(root.get("segmentType").in(request.getPlaceOptions()));
            }

            preds.add(cb.isNotNull(root.get("pointX")));
            preds.add(cb.isNotNull(root.get("pointY")));

            if (square != null) {
                List<Long> neighborIds = squareService.getNeighborSquares(square, 1).stream()
                        .map(SquareEntity::getId)
                        .collect(Collectors.toList());
                neighborIds.add(square.getId());
                preds.add(root.get("squareNum").in(neighborIds));
            }

            return cb.and(preds.toArray(new Predicate[0]));
        };
    }

    private List<RealtySummaryDTO> scoreAndPickTop5(List<RealtyEntity> src,
                                                    RealtySelectionRequestDTO req,
                                                    SquareEntity bestSquare) {
        Map<Long, Map<String, Double>> rubricCache = loadRubricScores(src);

        return src.stream()
                .map(re -> new Scored(re, calcScore(re, req, bestSquare, rubricCache)))
                .sorted(Comparator.comparingDouble(Scored::score).reversed())
                .limit(5)
                .map(sc -> toSummary(sc.entity))
                .toList();
    }

    private Map<Long, Map<String, Double>> loadRubricScores(List<RealtyEntity> src) {
        List<Long> ids = src.stream().map(RealtyEntity::getSquareNum).distinct().toList();

        return squareScoreRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(
                        sc -> sc.getSquareEntity().getId(),
                        sc -> safeCalc(() -> mapper.readValue(sc.getRubricScore(), new TypeReference<>() {}))
                ));
    }

    private double calcScore(RealtyEntity re,
                             RealtySelectionRequestDTO req,
                             SquareEntity bestSquare,
                             Map<Long, Map<String, Double>> rubricCache) {

        if (bestSquare == null) return 0;

        double dist = DistanceCalculator.calculateDistance(
                bestSquare.getCenterLat(), bestSquare.getCenterLon(),
                re.getPointX(), re.getPointX());

        double factor = 1 / (1 + dist);

        double s = factor;

        Map<String, Double> scores = rubricCache.get(re.getSquareNum());
        if (scores != null) {
            if (req.getWantToSee() != null)
                s += req.getWantToSee().stream()
                        .mapToDouble(r -> scores.getOrDefault(r, 0.0) * factor).sum();
            if (req.getDontWantToSee() != null)
                s -= req.getDontWantToSee().stream()
                        .mapToDouble(r -> scores.getOrDefault(r, 0.0) * factor).sum();
        }
        return s;
    }

    private List<PlaceMarkerDTO> getPlacesForRubrics(List<String> rubrics, PlaceMarkerDTO.MarkerType type) {
        if (rubrics == null || rubrics.isEmpty()) return List.of();
        String[] rubricsArray = rubrics.toArray(new String[0]);
        return placeRepository.findByRubrics(rubricsArray).stream()
                .map(p -> PlaceMarkerDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .address(p.getAddress())
                        .latitude(p.getLat())
                        .longitude(p.getLon())
                        .rubrics(p.getRubrics())
                        .markerType(type)
                        .build())
                .toList();
    }

    private record Scored(RealtyEntity entity, double score) { }

    private RealtySummaryDTO toSummary(RealtyEntity e) {
        return RealtySummaryDTO.builder()
                .id(e.getId())
                .address(e.getAddress())
                .mainType(e.getMainType())
                .segmentType(e.getSegmentType())
                .leasePrice(e.getLeasePrice())
                .latitude(e.getPointY())
                .longitude(e.getPointX())
                .totalArea(e.getTotalArea())
                .build();
    }

    private <T> Optional<T> opt(T val) {
        return Optional.ofNullable(val);
    }

    private <T> T safeCalc(ThrowingSupplier<T> s) {
        try { return s.get(); } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }
    @FunctionalInterface private interface ThrowingSupplier<T> { T get() throws Exception; }

}
