package krutomaps.backend.controller;

import jakarta.validation.Valid;
import krutomaps.backend.entity.PlaceEntity;
import krutomaps.backend.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/places")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class PlaceAdminController {

    private final PlaceRepository placeRepository;

    @GetMapping
    public List<PlaceEntity> list() {
        return placeRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceEntity> get(@PathVariable Long id) {
        return placeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PlaceEntity> create(@Valid @RequestBody PlaceEntity place) {
        place.setId(null);
        PlaceEntity saved = placeRepository.save(place);
        return ResponseEntity.created(URI.create("/admin/places/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaceEntity> update(@PathVariable Long id, @Valid @RequestBody PlaceEntity place) {
        if (!placeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        place.setId(id);
        return ResponseEntity.ok(placeRepository.save(place));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!placeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        placeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
