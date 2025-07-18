package krutomaps.backend.service;

import krutomaps.backend.entity.SquareEntity;
import krutomaps.backend.repository.SquareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SquareService {

    private final SquareRepository squareRepository;

    private static final int GRID_SIZE = 10;

    @Cacheable(value = "neighborSquares", key = "{#center.id, #radius}")
    @Transactional(readOnly = true)
    public List<SquareEntity> getNeighborSquares(SquareEntity center, int radius) {

        if (Objects.isNull(center) || radius < 0) {
            return Collections.emptyList();
        }

        int minI = Math.max(0, center.getI() - radius);
        int maxI = Math.min(GRID_SIZE - 1, center.getI() + radius);
        int minJ = Math.max(0, center.getJ() - radius);
        int maxJ = Math.min(GRID_SIZE - 1   , center.getJ() + radius);

        return squareRepository.findByIBetweenAndJBetween(minI, maxI, minJ, maxJ);
    }

    @Cacheable("allSquares")
    @Transactional(readOnly = true)
    public List<SquareEntity> getAllSquares() {
        return squareRepository.findAll();
    }

}
