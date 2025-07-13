package krutomaps.backend.controller;

import krutomaps.backend.entity.Place;
import krutomaps.backend.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceRepository placeRepository;

    @GetMapping("/rubrics")
    @Cacheable("rubricsCache")
    public List<String> getRubricsList() {
        List<Place> places = placeRepository.findAll();
        return places.stream()
                .flatMap(place -> place.getRubrics().stream())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }
}
