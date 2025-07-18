package krutomaps.backend.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import krutomaps.backend.entity.PlaceEntity;
import krutomaps.backend.entity.SquareEntity;
import krutomaps.backend.entity.SquareScoreEntity;
import krutomaps.backend.repository.PlaceRepository;
import krutomaps.backend.repository.SquareRepository;
import krutomaps.backend.repository.SquareScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@EnableRetry
public class SquareScoreCalculator {

    private final ObjectMapper mapper;
    private final PlaceRepository placeRepository;
    private final SquareRepository squareRepository;
    private final SquareScoreRepository squareScoreRepository;

    private static final double MAX_DISTANCE_KM = 2.0;
    private static final double SIGMA = 0.3;

    @Retryable(
        retryFor = ObjectOptimisticLockingFailureException.class,
        backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void recalc(Long squareId, List<String> allRubrics) throws JsonProcessingException {

        SquareEntity square = squareRepository.findById(squareId)
                .orElseThrow(() -> new RuntimeException("Square not found: " + squareId));

        Map<String, Double> rubricScoreMap = new HashMap<>();
        for (String rubric : allRubrics) {
            rubricScoreMap.put(rubric, calculateRubricScore(square, rubric));
        }

        String json = mapper.writeValueAsString(rubricScoreMap);
        SquareScoreEntity squareScore = squareScoreRepository.findById(squareId)
                .orElseGet(() -> SquareScoreEntity.builder()
                        .id(squareId)
                        .squareEntity(square)
                        .build());

        squareScore.setRubricScore(json);
        squareScoreRepository.save(squareScore);
    }

    private double calculateRubricScore(SquareEntity squareEntity, String rubric) {

        List<PlaceEntity> placeEntities = placeRepository.findByRubric(rubric);
        if (placeEntities.isEmpty()) return 0;

        double score = 0.0;
        for (PlaceEntity placeEntity : placeEntities) {
            if (placeEntity.getLat() == null || placeEntity.getLon() == null) continue;

            double distance = DistanceCalculator.calculateDistance(
                    squareEntity.getCenterLat(), squareEntity.getCenterLon(),
                    placeEntity.getLat(), placeEntity.getLon());

            if (distance <= MAX_DISTANCE_KM) {
                double weight = Math.exp(-distance * distance / (2 * SIGMA * SIGMA));
                score += weight;
            }
        }
        return score / (1 + score);
    }
}
