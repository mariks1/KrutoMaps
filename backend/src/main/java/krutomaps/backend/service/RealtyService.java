package krutomaps.backend.service;

import krutomaps.backend.dto.AreaRangeResponse;
import krutomaps.backend.dto.ChooseRealtyRequest;
import krutomaps.backend.dto.PriceRangeResponse;
import krutomaps.backend.entity.Realty;
import krutomaps.backend.repository.RealtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

@Service
@RequiredArgsConstructor
public class RealtyService {

    private final RealtyRepository realtyRepository;

    public PriceRangeResponse getPriceRange() {
        Double minPrice = realtyRepository.findMinPrice();
        Double maxPrice = realtyRepository.findMaxPrice();

        return PriceRangeResponse.builder()
                .minPrice(minPrice != null ? minPrice : 0)
                .maxPrice(maxPrice != null ? maxPrice : 0)
                .build();
    }

    public AreaRangeResponse getAreaRange() {
        Double minArea = realtyRepository.findMinArea();
        Double maxArea = realtyRepository.findMaxArea();

        return AreaRangeResponse.builder()
                .minArea(minArea != null ? minArea : 0)
                .maxArea(maxArea != null ? maxArea : 0)
                .build();
    }

    public List<Realty> findTop5ByCriteria(ChooseRealtyRequest request) {
        Specification<Realty> spec = buildSpecification(request);
        Pageable pageable = PageRequest.of(0, 5);
        return realtyRepository.findAll(spec, pageable).getContent();
    }

    private Specification<Realty> buildSpecification(ChooseRealtyRequest request) {
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


            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
