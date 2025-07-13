package krutomaps.backend.service;

import krutomaps.backend.entity.Square;
import krutomaps.backend.repository.SquareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SquareService {
    private final SquareRepository squareRepository;

    @Cacheable(value = "neighborSquares", key = "{#centerSquare.id, #radius}")
    public List<Square> getNeighborSquares(Square centerSquare, int radius) {
        int centerI = centerSquare.getI();
        int centerJ = centerSquare.getJ();

        int minI = Math.max(0, centerI - radius);
        int maxI = Math.min(9, centerI + radius);
        int minJ = Math.max(0, centerJ - radius);
        int maxJ = Math.min(9, centerJ + radius);

        return squareRepository.findByIBetweenAndJBetween(minI, maxI, minJ, maxJ);
    }

    @Cacheable("allSquares")
    public List<Square> getAllSquares() {
        return squareRepository.findAll();
    }

}
