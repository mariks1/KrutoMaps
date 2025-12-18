package krutomaps.backend.controller;

import jakarta.validation.Valid;
import krutomaps.backend.entity.RealtyEntity;
import krutomaps.backend.repository.RealtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/realty")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class RealtyAdminController {

    private final RealtyRepository realtyRepository;

    @GetMapping
    public List<RealtyEntity> list() {
        return realtyRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RealtyEntity> get(@PathVariable Long id) {
        return realtyRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RealtyEntity> create(@Valid @RequestBody RealtyEntity entity) {
        entity.setId(null);
        RealtyEntity saved = realtyRepository.save(entity);
        return ResponseEntity.created(URI.create("/admin/realty/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RealtyEntity> update(@PathVariable Long id, @Valid @RequestBody RealtyEntity entity) {
        if (!realtyRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        entity.setId(id);
        return ResponseEntity.ok(realtyRepository.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!realtyRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        realtyRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
