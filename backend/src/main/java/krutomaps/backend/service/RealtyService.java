package krutomaps.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import krutomaps.backend.dto.*;
import krutomaps.backend.entity.Realty;
import krutomaps.backend.entity.Square;
import krutomaps.backend.entity.SquareScore;
import krutomaps.backend.repository.PlaceRepository;
import krutomaps.backend.repository.RealtyRepository;
import krutomaps.backend.repository.SquareScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;

import static krutomaps.backend.utils.DistanceCalculator.calculateDistance;

@Service
@RequiredArgsConstructor
public class RealtyService {

    private final ObjectMapper mapper = new ObjectMapper();

    private final RealtyRepository realtyRepository;
    private final PlaceRepository placeRepository;
    private final SquareScoreRepository squareScoreRepository;
    private final SquareService squareService;

    @Cacheable("priceRange")
    public PriceRangeResponse getPriceRange() {
        Double minPrice = realtyRepository.findMinPrice();
        Double maxPrice = realtyRepository.findMaxPrice();

        return PriceRangeResponse.builder()
                .minPrice(minPrice != null ? minPrice : 0)
                .maxPrice(maxPrice != null ? maxPrice : 0)
                .build();
    }

    @Cacheable("areaRange")
    public AreaRangeResponse getAreaRange() {
        Double minArea = realtyRepository.findMinArea();
        Double maxArea = realtyRepository.findMaxArea();

        return AreaRangeResponse.builder()
                .minArea(minArea != null ? minArea : 0)
                .maxArea(maxArea != null ? maxArea : 0)
                .build();
    }

    public RealtySelectionResponse findTop5ByCriteria(RealtySelectionRequest request) throws JsonProcessingException {

        Specification<Realty> spec;
        Square bestSquare = null;

        if (!request.getWantToSee().isEmpty() || !request.getDontWantToSee().isEmpty()) {
            bestSquare = findBestSquare(request);

            List<Square> searchSquares = squareService.getNeighborSquares(bestSquare, 1);

            searchSquares.add(bestSquare);

             spec = buildSpecification(request)
                    .and((root, query, cb) -> root.get("squareNum").in(
                            searchSquares.stream().map(Square::getId).collect(Collectors.toList())
                    ));
        } else {
             spec = buildSpecification(request);
        }
        List<Realty> candidates = realtyRepository.findAll(spec);

        List<ScoredRealty> scoredList = calculateRealtyScores(candidates, request, bestSquare);

        List<Realty> topRealty = scoredList.stream()
                .sorted(Comparator.comparingDouble(ScoredRealty::score).reversed())
                .limit(5)
                .map(ScoredRealty::realty)
                .collect(Collectors.toList());

        List<PlaceMarker> preferredPlaces = getPlacesForRubrics(request.getWantToSee(), "preferred");
        List<PlaceMarker> avoidedPlaces = getPlacesForRubrics(request.getDontWantToSee(), "avoided");

        return RealtySelectionResponse.builder()
                .realtyList(topRealty)
                .preferredPlaces(preferredPlaces)
                .avoidedPlaces(avoidedPlaces)
                .build();
    }


    private Square findBestSquare(RealtySelectionRequest request) {
        return squareService.getAllSquares().stream()
                .max(Comparator.comparingDouble(square -> {
                    try {
                        return calculateSquareSelectionScore(square, request);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }))
                .orElseThrow(() -> new IllegalStateException("No squares found"));
    }

    private double calculateSquareSelectionScore(Square square, RealtySelectionRequest request) throws JsonProcessingException {

        SquareScore squareScore = squareScoreRepository.findById(square.getId())
                .orElseGet(() -> SquareScore.builder()
                        .id(square.getId())
                        .rubricScore("{}")
                        .build());

        Map<String, Double> rubricScores = mapper.readValue(
                squareScore.getRubricScore(),
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

    public record ScoredRealty(Realty realty, double score) {}

    public List<ScoredRealty> calculateRealtyScores(List<Realty> candidates, RealtySelectionRequest request, Square bestSquare) {
        if (bestSquare == null) {
            return candidates.parallelStream().map(realty -> new ScoredRealty(realty, 0)).collect(Collectors.toList());
        }

        List<Long> squareIds = candidates.stream()
                .map(Realty::getSquareNum)
                .distinct()
                .toList();

        List<SquareScore> squareScores = squareScoreRepository.findAllById(squareIds);

        Map<Long, Map<String, Double>> rubricScoresMap = new HashMap<>();

        for (SquareScore score : squareScores) {
            try {
                Map<String, Double> rubricScores = mapper.readValue(
                        score.getRubricScore(),
                        new TypeReference<>() {}
                );
                rubricScoresMap.put(score.getSquare().getId(), rubricScores);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return candidates.parallelStream().map(realty -> {
            double score = 0.0;

            double distanceToCenter = calculateDistance(
                    bestSquare.getCenter_lat(), bestSquare.getCenter_lon(),
                    realty.getPointX(), realty.getPointY()
            );
            double distanceFactor = 1.0 / (1 + distanceToCenter);
            score += distanceFactor;

            Map<String, Double> rubricScores = rubricScoresMap.get(realty.getSquareNum());
            if (rubricScores != null) {
                if (request.getWantToSee() != null) {
                    for (String rubric : request.getWantToSee()) {
                        Double rubricScore = rubricScores.get(rubric);
                        if (rubricScore != null) {
                            score += rubricScore * distanceFactor;
                        }
                    }
                }

                if (request.getDontWantToSee() != null) {
                    for (String rubric : request.getDontWantToSee()) {
                        Double rubricScore = rubricScores.get(rubric);
                        if (rubricScore != null) {
                            score -= rubricScore * distanceFactor;
                        }
                    }
                }
            }

            return new ScoredRealty(realty, score);
        }).collect(Collectors.toList());

    }

    private List<PlaceMarker> getPlacesForRubrics(List<String> rubrics, String markerType) {
        if (rubrics == null || rubrics.isEmpty()) {
            return Collections.emptyList();
        }

        String rubricsArray = "{" + rubrics.stream()
                .map(r -> "\"" + r.replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(",")) + "}";

        return placeRepository.findByRubrics(rubricsArray).stream()
                .map(place -> PlaceMarker.builder()
                        .id(place.getId())
                        .name(place.getName())
                        .address(place.getAddress())
                        .lat(place.getLat())
                        .lon(place.getLon())
                        .rubrics(place.getRubrics())
                        .markerType(markerType)
                        .build())
                .collect(Collectors.toList());
    }
    private Specification<Realty> buildSpecification(RealtySelectionRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getPriceFrom() != null && request.getPriceFrom() > 0) {
                predicates.add(cb.ge(root.get("leasePrice"), request.getPriceFrom()));
            }

            if (request.getPriceTo() != null && request.getPriceTo() > 0) {
                predicates.add(cb.le(root.get("leasePrice"), request.getPriceTo()));
            }

            String floorOption = request.getFloorOption();
            if (floorOption != null && !floorOption.equals("Любой") && !floorOption.equals("Не последний")) {
                switch (floorOption) {
                    case "Только 1-й" -> predicates.add(cb.equal(root.get("floor"), 1));
                    case "До 5-го" -> predicates.add(cb.le(root.get("floor"), 5));
                    case "Не 1-й" -> predicates.add(cb.notEqual(root.get("floor"), 1));
                }
            }

            List<String> placeOptions = request.getPlaceOptions();
            if (placeOptions != null && !placeOptions.contains("Любой")) {
                predicates.add(root.get("segmentType").in(placeOptions));
            }

            if (request.getAreaFrom() != null && request.getAreaFrom() > 0) {
                predicates.add(cb.ge(root.get("totalArea"), request.getAreaFrom()));
            }

            if (request.getAreaTo() != null && request.getAreaTo() > 0) {
                predicates.add(cb.le(root.get("totalArea"), request.getAreaTo()));
            }

            predicates.add(cb.isNotNull(root.get("pointX")));
            predicates.add(cb.isNotNull(root.get("pointY")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
