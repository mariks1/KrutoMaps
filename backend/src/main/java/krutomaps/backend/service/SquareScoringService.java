package krutomaps.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import krutomaps.backend.entity.Place;
import krutomaps.backend.entity.Square;
import krutomaps.backend.entity.SquareScore;
import krutomaps.backend.repository.PlaceRepository;
import krutomaps.backend.repository.SquareRepository;
import krutomaps.backend.repository.SquareScoreRepository;
import krutomaps.backend.utils.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SquareScoringService {

    private static final double MAX_DISTANCE_KM = 2.0;
    private static final double SCORE_DECAY = 0.5;

    private final ObjectMapper mapper;
    private final PlaceRepository placeRepository;
    private final SquareRepository squareRepository;
    private final SquareScoreRepository squareScoreRepository;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(cron = "0 0 4 * * ?")
    public void updateAllSquareScores() {
        System.out.println("Updating all square scores");

        List<Long> squareIds = squareRepository.findAllIds();
        List<String> allRubrics = placeRepository.findAllUniqueRubrics();

        for (Long squareId : squareIds) {
            transactionTemplate.executeWithoutResult(status -> {
                try {
                    updateSquareScores(squareId, allRubrics);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                } catch (ObjectOptimisticLockingFailureException e) {
                    transactionTemplate.executeWithoutResult(innerStatus -> {
                        try {
                            updateSquareScores(squareId, allRubrics);
                        } catch (JsonProcessingException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                }
            });
        }
    }

    public void updateSquareScores(Long squareId, List<String> allRubrics) throws JsonProcessingException {

        Square square = squareRepository.findById(squareId)
                .orElseThrow(() -> new RuntimeException("Square not found: " + squareId));

        Map<String, Double> rubricScoreMap = new HashMap<>();
        for (String rubric : allRubrics) {
            double score = calculateRubricScore(square, rubric);
            rubricScoreMap.put(rubric, score);
        }

        String json = mapper.writeValueAsString(rubricScoreMap);

        squareScoreRepository.findById(square.getId()).ifPresentOrElse(
                existingScore -> {
                    existingScore.setRubricScore(json);
                    squareScoreRepository.save(existingScore);
                },
                () -> {
                    SquareScore newScore = new SquareScore();
                    newScore.setSquare(square);
                    newScore.setRubricScore(json);
                    squareScoreRepository.save(newScore);
                }
        );
    }

    private double calculateRubricScore(Square square, String rubric) {
        List<Place> places = placeRepository.findByRubric(rubric);
        double score = 0;

        System.out.println("Calculating score for " + rubric);
        System.out.println(places.toString());

        for (Place place : places) {
            if (place.getLat() == null || place.getLon() == null) {
                System.err.println("Place with null coordinates: " + place.getId());
                continue;
            }
            double distance = DistanceCalculator.calculateDistance(square.getCenter_lat(), square.getCenter_lon(), place.getLat(), place.getLon());
            if (distance <= MAX_DISTANCE_KM) {
                // double weight = Math.exp(-distance * distance / (2 * sigma * sigma)); sigma = 0.3
                double weight = 1 / (1 + Math.pow(distance, 2) * SCORE_DECAY);
                score += weight;
            }
        }
        return score / (1 + score);
    }



}
