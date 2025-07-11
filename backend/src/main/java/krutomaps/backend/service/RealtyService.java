package krutomaps.backend.service;

import krutomaps.backend.dto.*;
import krutomaps.backend.entity.Realty;
import krutomaps.backend.repository.PlaceRepository;
import krutomaps.backend.repository.RealtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;

@Service
@RequiredArgsConstructor
public class RealtyService {

    private final RealtyRepository realtyRepository;
    private final PlaceRepository placeRepository;

    private static final int BATCH_SIZE = 1000;
    private static final double WANT_COEFFICIENT = 1.0;
    private static final double DONT_COEFFICIENT = -3.0;

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

    public RealtySelectionResponse findTop5ByCriteria(RealtySelectionRequest request) {
        Specification<Realty> spec = buildSpecification(request);
        Pageable page = PageRequest.of(0, 100);
        List<Realty> candidates = realtyRepository.findAll(spec, page).getContent();

        List<ScoredRealty> scoredList = calculateScores(candidates, request);
        List<Realty> topRealty = scoredList.stream()
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .limit(5)
                .map(ScoredRealty::realty)
                .toList();

        List<PlaceMarker> preferredPlaces = getPlacesForRubrics(request.getWantToSee(), "preferred");
        List<PlaceMarker> avoidedPlaces = getPlacesForRubrics(request.getDontWantToSee(), "avoided");

        return RealtySelectionResponse.builder()
                .realtyList(topRealty)
                .preferredPlaces(preferredPlaces)
                .avoidedPlaces(avoidedPlaces)
                .build();

    }

    private List<PlaceMarker> getPlacesForRubrics(List<String> rubrics, String markerType) {
        if (rubrics == null || rubrics.isEmpty()) {
            return Collections.emptyList();
        }


        String rubricsArray = "{" + rubrics.stream().map(r -> "\"" + r.replace("\"", "\\\"") + "\"").collect(Collectors.joining(",")) + "}";


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

    public record ScoredRealty(Realty realty, double score) {}

    public List<ScoredRealty> calculateScores(List<Realty> candidates, RealtySelectionRequest request) {
        List<Long> candidateIds = candidates.stream().map(Realty::getId).collect(Collectors.toList());

        Map<Long, Map<String, Double>> wantDistances = getMinDistancesForRubrics(candidateIds, request.getWantToSee());
        Map<Long, Map<String, Double>> dontWantDistances = getMinDistancesForRubrics(candidateIds, request.getDontWantToSee());

        return candidates.parallelStream().map(candidate -> {
            double score = 0.0;

            if (candidate.getPoint_x() == null || candidate.getPoint_y() == null) {
                return new ScoredRealty(candidate, Double.NEGATIVE_INFINITY);
            }

            if (request.getWantToSee() != null && !request.getWantToSee().isEmpty()) {
                Map<String, Double> distances = wantDistances.getOrDefault(candidate.getId(), Collections.emptyMap());
                for (String rubric : request.getWantToSee()) {
                    Double minDist = distances.get(rubric);
                    if (minDist != null) {
                        score += WANT_COEFFICIENT / (1 + minDist / 1000);
                    }
                }
            }

            if (request.getDontWantToSee() != null && !request.getDontWantToSee().isEmpty()) {
                Map<String, Double> distances = dontWantDistances.getOrDefault(candidate.getId(), Collections.emptyMap());
                for (String rubric : request.getDontWantToSee()) {
                    Double minDist = distances.get(rubric);
                    if (minDist != null && minDist < 2000) {
                        score += DONT_COEFFICIENT * (1 - minDist / 2000);
                    }
                }
            }

            return new ScoredRealty(candidate, score);
        }).collect(Collectors.toList());
    }


    public Map<Long, Map<String, Double>> getMinDistancesForRubrics(List<Long> candidateIds, List<String> rubrics) {
        Map<Long, Map<String, Double>> result = new HashMap<>();
        for (int i = 0; i < candidateIds.size(); i += BATCH_SIZE) {
            List<Long> batch = candidateIds.subList(i, Math.min(i + BATCH_SIZE, candidateIds.size()));
            String rubricsArray = "{" + rubrics.stream().map(r -> "\"" + r.replace("\"", "\\\"") + "\"").collect(Collectors.joining(",")) + "}";
            List<Object[]> batchResults = realtyRepository.findMinDistancesForRubrics(batch, rubricsArray);
            for (Object[] row : batchResults) {
                Long realtyId = (Long) row[0];
                String rubric = (String) row[1];
                Double minDist = (Double) row[2];
                result.computeIfAbsent(realtyId, k -> new HashMap<>()).put(rubric, minDist);
            }
        }
        return result;
    }

    private Specification<Realty> buildSpecification(RealtySelectionRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getPriceFrom() != null && request.getPriceFrom() > 0) {
                predicates.add(cb.ge(root.get("lease_price"), request.getPriceFrom()));
            }

            if (request.getPriceTo() != null && request.getPriceTo() > 0) {
                predicates.add(cb.le(root.get("lease_price"), request.getPriceTo()));
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
                predicates.add(root.get("segment_type").in(placeOptions));
            }

            if (request.getAreaFrom() != null && request.getAreaFrom() > 0) {
                predicates.add(cb.ge(root.get("total_area"), request.getAreaFrom()));
            }

            if (request.getAreaTo() != null && request.getAreaTo() > 0) {
                predicates.add(cb.le(root.get("total_area"), request.getAreaTo()));
            }

            predicates.add(cb.isNotNull(root.get("point_x")));
            predicates.add(cb.isNotNull(root.get("point_y")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
