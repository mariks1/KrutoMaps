    package krutomaps.backend.service;

    import com.fasterxml.jackson.core.JsonProcessingException;
    import krutomaps.backend.repository.PlaceRepository;
    import krutomaps.backend.repository.SquareRepository;
    import krutomaps.backend.utils.SquareScoreCalculator;
    import lombok.RequiredArgsConstructor;
    import org.springframework.scheduling.annotation.Scheduled;
    import org.springframework.stereotype.Service;

    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class SquareScoringService {

        private final PlaceRepository placeRepository;
        private final SquareRepository squareRepository;
        private final SquareScoreCalculator calculator;

        @Scheduled(cron = "0 15 19 * * ?")
        public void updateAllSquareScores() {

            List<Long> squareIds   = squareRepository.findAllIds();
            List<String> allRubrics = placeRepository.findAllUniqueRubrics();

            squareIds.forEach(id -> {
                try {
                    calculator.recalc(id, allRubrics);
                } catch (JsonProcessingException e) {
                    // TODO: логирование/метрики
                }
            });
        }

    }
